package com.mbc.mobileapp.command.user.pincode;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.ComForgotPassHist;
import com.mbc.common.entity.Cust;
import com.mbc.common.entity.ImIeUser;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComForgotPassHistRepo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.repository.ImIeUserRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoCheckCustomerInfo implements Command {

    private final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ImIeUserRepo imIeUserRepo;

    @Autowired
    private ComForgotPassHistRepo forgotPassHistRepository;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();
        try {
            Cust cust = null;
            if(Constant.SrvcCd.SRVC_USER_FORGOT_PINCODE.equals(request.getSrvcCd())){
                ComForgotPassHist passHist = forgotPassHistRepository.getForgotPassHist(request.getTransId(), Constant.COM_STATUS_INT);
                cust = custRepo.findByIdTypNoAndIsDelete(passHist.getIdCardNumber(), Constant.NO);
            }else{
                cust = custRepo.findByIdTypNoAndIsDelete(request.getIdTypNo(), Constant.NO);
            }
            if(Objects.nonNull(cust)) {
                ImIeUser imIeUser = imIeUserRepo.findByCd(cust.getSpiUsrCd());
                custInfo = mapper.convertValue(cust, CustInfo.class);
                custInfo.setGender(cust.getGenderCd());
                custInfo.setPhotoStr(cust.getPhoto());
                custInfo.setImUserStatus(imIeUser.getStatus());
                custInfo.setImIeUser(imIeUser);

                if (Constant.YES.equals(cust.getIsInactive())) {
                    result = new SimpleResult(ResponseCode.CUSTOMER_INACTIVE.getDesc(), false, ResponseCode.CUSTOMER_INACTIVE.getCode());
                    processContext.setResult(result);
                    return !result.isOk();
                }

                if(!Constant.SrvcCd.SRVC_USER_FORGOT_PINCODE.equals(request.getSrvcCd())){
                    if (!request.getPhoneNo().equals(cust.getPhoneNo())) {
                        result = new SimpleResult(MBCResponseCode.PHONE_NO_EXITSTED.getDesc(), false,
                                MBCResponseCode.PHONE_NO_EXITSTED.getCode());
                        processContext.setResult(result);
                        return !result.isOk();
                    }
                }



            }else{
                result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
            }

        }catch (Exception e){
            AppLog.error("[SDK Exception Set Pin Code] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }
        processContext.setCustomer(custInfo);
        processContext.setResult(result);
        return !result.isOk();
    }
}
