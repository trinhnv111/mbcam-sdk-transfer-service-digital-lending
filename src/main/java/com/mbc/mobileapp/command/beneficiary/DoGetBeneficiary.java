package com.mbc.mobileapp.command.beneficiary;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.dto.ComBeneficiaryDTO;
import com.mbc.common.entity.ComBeneficiary;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComBeneficiaryRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoGetBeneficiary implements Command {

    @Autowired
    private ComBeneficiaryRepo comBeneficiaryRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        ComBeneficiaryDTO comBeneficiaryDTOTemp = new ComBeneficiaryDTO();
        comBeneficiaryDTOTemp.setCustId(customer.getId());

        try {
            List<ComBeneficiary> lstComBeneficiary =
                comBeneficiaryRepo.findByCustIdAndIsDeleteOrderByUpdatedDtDesc(customer.getId(), Constant.NO);

            List<ComBeneficiary> beneficiaryDTOList = new ArrayList<ComBeneficiary>();
            if(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER.equals(request.getSrvcCd())){
                for (ComBeneficiary comBeneficiary : lstComBeneficiary){
                    if("REMITTANCE".equals(comBeneficiary.getPartner())){
                        beneficiaryDTOList.add(comBeneficiary);
                    }
                }
            }

            if(Constant.SrvcCd.SRVC_TRANS_INHOUSE.equals(request.getSrvcCd())){
                for (ComBeneficiary comBeneficiary : lstComBeneficiary){
                    if("MBC".equals(comBeneficiary.getPartner()) || Utility.isNull(comBeneficiary.getPartner())){
                        beneficiaryDTOList.add(comBeneficiary);
                    }
                }
            }
            if(Constant.SrvcCd.SRVC_TRANS_CIFTP_CASA.equals(request.getSrvcCd()) || Constant.SrvcCd.SRVC_TRANS_CIFTP_WALLET.equals(request.getSrvcCd())){
                for (ComBeneficiary comBeneficiary : lstComBeneficiary){
                    if("BAKONG".equals(comBeneficiary.getPartner()) || "CIFTP".equals(comBeneficiary.getPartner())){
                        beneficiaryDTOList.add(comBeneficiary);
                    }
                }
            }

            if(Utility.isNull(request.getSrvcCd())){
                beneficiaryDTOList = lstComBeneficiary;
            }

            context.putVar("beneficiaryDTOList", beneficiaryDTOList);
        }
        catch (Exception e) {
            AppLog.error("ERROR: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
