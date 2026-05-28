package com.mbc.mobileapp.command.beneficiary;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.ComBeneficiary;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComBeneficiaryRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import jodd.util.StringUtil;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;


@Service
public class DoSaveBeneficiary implements Command {

    @Autowired
    private ComBeneficiaryRepo comBeneficiaryRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        String[] partners = {"BAKONG", "FAST", "MBC", "REMITTANCE", "CIFTP"};

        try {

            if (!"WALLET".equals(request.getTransType().toUpperCase()) && (StringUtil.isEmpty(request.getBenAcctNo()) || StringUtil.isEmpty(request.getBenBankCode()))) {
                result = new SimpleResult(MBCResponseCode.BANKCODE_AND_BANK_ACCTNO_NOT_NULL.getDesc(), false,
                        MBCResponseCode.BANKCODE_AND_BANK_ACCTNO_NOT_NULL.getCode());
            } else {
                List<ComBeneficiary> checkExistsBeneficiary;
                if (!"WALLET".equals(request.getTransType().toUpperCase())) {
                    checkExistsBeneficiary = comBeneficiaryRepo.findByCustIdAndIsDeleteAndBenBankCodeAndBenAcctNoAndPartner(
                            customer.getId(), Constant.NO,
                            request.getBenBankCode(),
                            request.getBenAcctNo(),
                            request.getPartnerCode());

                } else {
                    checkExistsBeneficiary = comBeneficiaryRepo.findByIsDeleteAndBenAcctNoAndPartnerAndCustId(Constant.NO, request.getBenAcctNo(), request.getPartnerCode(),customer.getId());

                }
                ComBeneficiary comBeneficiaryTemp = null;
                if (CollectionUtils.isEmpty(checkExistsBeneficiary)) {

                    comBeneficiaryTemp = new ComBeneficiary();

                    List<String> partners_ = Arrays.asList(partners);
                    if (partners_.contains(request.getPartnerCode().toUpperCase())) {
                        comBeneficiaryTemp.setPartner(request.getPartnerCode());
                    } else {
                        result = new SimpleResult(MBCResponseCode.INCORRECT_PARTNER.getDesc(), false,
                                MBCResponseCode.INCORRECT_PARTNER.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }

                    comBeneficiaryTemp.setId(request.getBeneficiaryId());
                    comBeneficiaryTemp.setCustId(customer.getId());
                    comBeneficiaryTemp.setBenAcctNo(request.getBenAcctNo());
                    comBeneficiaryTemp.setBenAcctName(request.getBenAcctName());
                    comBeneficiaryTemp.setBenBankMnemonic(request.getBenBankMnemonic());

                    comBeneficiaryTemp.setBenBankCode(request.getBenBankCode());
                    comBeneficiaryTemp.setCcyCd(request.getCurrency());

                    comBeneficiaryTemp.setBenBankName(request.getBenBankName());
                    comBeneficiaryTemp.setType(request.getType());
                    comBeneficiaryTemp.setTransType(request.getTransType().toLowerCase());
                    comBeneficiaryTemp.setSuggestName(request.getSuggestName());
                    comBeneficiaryTemp.setIsDelete(Constant.NO);
                    comBeneficiaryTemp.setCreatedBy(request.getUserId());
                } else {
                    comBeneficiaryTemp = checkExistsBeneficiary.get(0);
                    comBeneficiaryTemp.setUpdatedBy(request.getUserId());
                    if (!StringUtils.isEmpty(request.getSuggestName())) {
                        comBeneficiaryTemp.setSuggestName(request.getSuggestName());
                    }
                    if (!StringUtils.isEmpty(request.getBenBankName())) {
                        comBeneficiaryTemp.setBenBankName(request.getBenBankName());
                    }
                    if (!StringUtils.isEmpty(request.getBenAcctName())) {
                        comBeneficiaryTemp.setBenAcctName(request.getBenAcctName());
                    }
                    if (!StringUtils.isEmpty(request.getCurrency())) {
                        comBeneficiaryTemp.setCcyCd(request.getCurrency());
                    }
                    if (!StringUtils.isEmpty(request.getBenBankMnemonic())) {
                        comBeneficiaryTemp.setBenBankMnemonic(request.getBenBankMnemonic());
                    }
                    if (!StringUtils.isEmpty(request.getType())) {
                        comBeneficiaryTemp.setType(request.getType());
                    }
                    if (!StringUtils.isEmpty(request.getTransType())) {
                        comBeneficiaryTemp.setTransType(request.getTransType().toLowerCase());
                    }
                }
                comBeneficiaryRepo.save(comBeneficiaryTemp);

            }
        } catch (Exception e) {
            AppLog.error("ERROR: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
