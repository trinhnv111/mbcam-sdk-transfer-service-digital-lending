package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.api.ApiFundsTransfer;
import com.mbc.common.api.models.fundsTransfer.*;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTrans;
import com.mbc.common.entity.ComTransDtlLoanDisbursement;
import com.mbc.common.entity.ComTransProcess;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanDisbursementRepo;
import com.mbc.common.repository.ComTransProcessRepo;
import com.mbc.common.repository.ComTransRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.RedisServer;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.ServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoDisbursement implements Command {
    private final ApiFundsTransfer apiFundsTransfer;
    private final ComTransRepo comTransRepo;
    private final ComTransDtlLoanDisbursementRepo comTransDtlLoanDisbursementRepo;
    private final ComTransProcessRepo comTransProcessRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();
        ComTrans comTrans;
        ComTransDtlLoanDisbursement comTransDtlLoanDisbursement;

        try {
            comTrans = comTransRepo.findByCustIdAndSrvcCdAndStatusAndIdAndSessionId(custInfo.getId(), request.getSrvcCd(), Constant.COM_STATUS_INT, request.getTransId(), request.getSessionId());
            if (Objects.nonNull(comTrans)) {
                comTransDtlLoanDisbursement = comTransDtlLoanDisbursementRepo.findById(comTrans.getId()).get();
                ExecuteT24Output<DataOutput> output = executeTransfer(comTrans, comTransDtlLoanDisbursement, custInfo.getId(), request.getRequestId());
                if (Objects.isNull(output)) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getCode(), false, ResponseCode.REQUEST_TIMEOUT.getDesc());
                } else {
                    if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                        if ("002".equals(output.getSoaErrorCode())) {
                            result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getCode(), false, ResponseCode.REQUEST_TIMEOUT.getDesc());
                        } else {
                            String errorDesc = output.getErrorInfo().getErrorDesc();
                            if (!Utility.isNull(output.getErrorInfo().getErrorDetail())) {
                                errorDesc = output.getErrorInfo().getErrorDesc() + " - " + output.getErrorInfo().getErrorDetail();
                            }
                            result = new SimpleResult(errorDesc, false, output.getErrorInfo().getErrorCode());
                        }
                    } else {
                        response.setFt(output.getData().getT24Ft());
                        RedisServer.removeCacheRedis(ServiceConstant.REDIS_KEY_LIST_LOAN + request.getSessionId());
                    }
                }
            } else {
                result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
            }

        } catch (Exception e) {
            log.info("[Exception OD Disbursement] requestId: {} desc: {}", request.getRequestId(), e.toString());
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        context.setResponse(response);
        return !result.isOk();
    }

    private ExecuteT24Output<DataOutput> executeTransfer(ComTrans infoTrans, ComTransDtlLoanDisbursement comTransDtlLoanDisbursement, String custId, String requestId) throws Exception {
        FundsTransferInfo transferInfo = new FundsTransferInfo();
        CreditAccount creditAccount = new CreditAccount();
        creditAccount.setAccountName(comTransDtlLoanDisbursement.getCrebitAcctName());
        creditAccount.setAccountNumber(comTransDtlLoanDisbursement.getCrebitAcctNo());
        creditAccount.setAccountCurrency(comTransDtlLoanDisbursement.getCrebitAcctCcy());
        creditAccount.setAccountType("ACCOUNT");
        DebitAccount debitAccount = new DebitAccount();
        debitAccount.setAccountNumber(comTransDtlLoanDisbursement.getDebitAcctNo());
        debitAccount.setAccountName(comTransDtlLoanDisbursement.getDebitAcctName());
        debitAccount.setAccountCurrency(comTransDtlLoanDisbursement.getDebitAcctCcy());
        debitAccount.setAccountType("ACCOUNT");
        Amount amount = new Amount();
        amount.setAmount(comTransDtlLoanDisbursement.getDebitAmount());
        amount.setCurrency(comTransDtlLoanDisbursement.getDebitAcctCcy());
        CreditBank creditBank = new CreditBank();
        transferInfo.setAmount(amount);
        transferInfo.setBranchCode(comTransDtlLoanDisbursement.getBranchCode());
        transferInfo.setChargeInfo(null);
        transferInfo.setCreditAccount(creditAccount);
        transferInfo.setCreditBank(creditBank);
        transferInfo.setDebitAccount(debitAccount);
        transferInfo.setRemark(infoTrans.getDescription());
        transferInfo.setTransferType(comTransDtlLoanDisbursement.getTransactionType());
        AddInfoList addInfoList = new AddInfoList();
        addInfoList.setName("ORDERING_BANK");
        addInfoList.setValue("MBC");
        List<AddInfoList> listAddInfo = new ArrayList<AddInfoList>();
        listAddInfo.add(addInfoList);
        transferInfo.setAddInfoList(listAddInfo);

        ExecuteT24Output<DataOutput> esbOutput = apiFundsTransfer.makeTransfer(transferInfo, custId, requestId, infoTrans.getId());
        if (esbOutput != null) {
            // ====== hard code error start =======
            // esbOutput.setErrorCode("403");
            // esbOutput.setSoaErrorDesc("Record is locked");
            // ErrorInfo errorInfo = new ErrorInfo();
            // errorInfo.setErrorCode("403");
            // errorInfo.setErrorDesc("Record is locked");
            // esbOutput.setErrorInfo(errorInfo);
            // esbOutput.setStatus("423");
            // ====== hard code error end =======
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(esbOutput.getStatus())) {
                infoTrans.setFt(esbOutput.getData().getT24Ft());
                infoTrans.setStatus(Constant.COM_STATUS_COM);
                comTransRepo.saveAndFlush(infoTrans);
                comTransDtlLoanDisbursement.setFt(esbOutput.getData().getT24Ft());
                comTransDtlLoanDisbursement.setStatus(Constant.COM_STATUS_COM);
                comTransDtlLoanDisbursement.setRequestId(requestId);
                comTransDtlLoanDisbursementRepo.saveAndFlush(comTransDtlLoanDisbursement);
                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setFt(esbOutput.getData().getT24Ft());
                comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
                comTransProcess.setTransId(infoTrans.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_COM);
                comTransProcess.setErrorCode(esbOutput.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(esbOutput.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            } else {
                infoTrans.setStatus(Constant.COM_STATUS_FAIL);
                comTransRepo.saveAndFlush(infoTrans);
                comTransDtlLoanDisbursement.setRequestId(requestId);
                comTransDtlLoanDisbursement.setStatus(Constant.COM_STATUS_FAIL);
                comTransDtlLoanDisbursementRepo.saveAndFlush(comTransDtlLoanDisbursement);
                ComTransProcess comTransProcess = new ComTransProcess();
                comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
                comTransProcess.setTransId(infoTrans.getId());
                comTransProcess.setStatus(Constant.COM_STATUS_FAIL);
                comTransProcess.setErrorCode(esbOutput.getErrorInfo().getErrorCode());
                comTransProcess.setErrorDesc(esbOutput.getErrorInfo().getErrorDesc());
                comTransProcessRepo.saveAndFlush(comTransProcess);
            }
        } else {
            infoTrans.setStatus(Constant.COM_STATUS_PND);
            comTransRepo.saveAndFlush(infoTrans);
            comTransDtlLoanDisbursement.setRequestId(requestId);
            comTransDtlLoanDisbursement.setStatus(Constant.COM_STATUS_PND);
            comTransDtlLoanDisbursementRepo.saveAndFlush(comTransDtlLoanDisbursement);
            ComTransProcess comTransProcess = new ComTransProcess();
            comTransProcess.setSrvcCd(infoTrans.getSrvcCd());
            comTransProcess.setTransId(infoTrans.getId());
            comTransProcess.setStatus(Constant.COM_STATUS_PND);
            comTransProcessRepo.saveAndFlush(comTransProcess);
        }
        return esbOutput;
    }

}
