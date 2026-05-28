package com.mbc.mobileapp.command.rm;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.Cust;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.rm.RmCodeOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class DoGetRmInfo implements Command {

//    @Autowired
//    private ApiRm apiRm;
    
    @Autowired
    private CustRepo custRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
//        CustInfo custInfo = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
//        ExecuteT24Output<List<RmCodeOutput>> outpuEsb = null;
        
//        String custId = custInfo != null ? custInfo.getId() : null;
        
        try {
//            RmInfoMessageInput message = new RmInfoMessageInput();
//            message.setRm(request.getRmCode());        
//            outpuEsb = apiRm.getRm(message, custId, request.getRequestId());
//
//            if (outpuEsb != null) {
//                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(outpuEsb.getStatus())) {
//                    response.setRmCodeOutput(outpuEsb.getData());
//                }
//                else if("500".equals(outpuEsb.getStatus()) && "4144".equals(outpuEsb.getErrorInfo().getErrorCode())) {
//                    
//                    
//                    
//                }
//                else {
//                    result = new SimpleResult(
//                        outpuEsb.getErrorInfo().getErrorDesc() + " - " + outpuEsb.getErrorInfo().getErrorDetail(),
//                        false, outpuEsb.getErrorInfo().getErrorCode());
//                }
//            }
            
            List<Cust> lstCust = custRepo.findByPhoneNoAndIsDelete(request.getRmMobile(), Constant.NO);
            List<RmCodeOutput> lstRmCodeOutput = new ArrayList<>();
            if(!lstCust.isEmpty()) {
                for (Cust cust : lstCust) {
                    RmCodeOutput rmCodeOutput = new RmCodeOutput();
                    rmCodeOutput.setName(cust.getNm());
                    rmCodeOutput.setRmMobile(cust.getPhoneNo());
                    rmCodeOutput.setRmCif(cust.getHostCifId());
                    lstRmCodeOutput.add(rmCodeOutput);
                }
                response.setLstRmCodeOutput(lstRmCodeOutput);
            }else {
                result = new SimpleResult(MBCResponseCode.RM_IS_EMPTY.getDesc(), false,
                    MBCResponseCode.RM_IS_EMPTY.getCode());
            }
        }
        catch (Exception e) {
            AppLog.error("[SDK Exception Get RM Info]: requestId: "+request.getRequestId()+", desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

}
