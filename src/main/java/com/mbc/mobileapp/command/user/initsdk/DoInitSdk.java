package com.mbc.mobileapp.command.user.initsdk;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.Cust;
import com.mbc.common.entity.ImIeUser;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.CustCustomRepo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.repository.ImIeUserRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.user.initsdk.CustomerInfo;
import com.mbc.mobileapp.rest.user.initsdk.InitSdkInfo;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoInitSdk implements Command {

    private final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ImIeUserRepo imIeUserRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
         CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        // context.getResponse();
        CustInfo custInfo = null;
        InitSdkInfo initSdkInfo = request.getInitSdkInfo();

        try{
//            Cust cust = custRepo.findByIdTypNoAndPhoneNo(initSdkInfo.getIdCardNumber(), initSdkInfo.getPhoneNumber());
            Cust cust = custRepo.findByIdTypNo(initSdkInfo.getIdCardNumber());

            if(Objects.nonNull(cust)){

                if(Constant.NO.equals(cust.getIsDelete())){
                    ImIeUser user = imIeUserRepo.findByCd(cust.getSpiUsrCd());

                    custInfo = mapper.convertValue(cust, CustInfo.class);
                    custInfo.setGender(cust.getGenderCd());
                    custInfo.setPhotoStr(cust.getPhoto());
                    custInfo.setImUserStatus(user.getStatus());
                    custInfo.setImIeUser(user);

                    if (!"N".equalsIgnoreCase(cust.getIsInactive())) {
                        custInfo.setState(Constant.MB_CUSTOMER_STATE_NOTACTIVE);
                    }

                    else if (Constant.USER_STATUS_AUTO_LOCKED.equals(user.getStatus())) {
                        custInfo.setState(Constant.MB_CUSTOMER_STATE_AUTO_LOCKED);
                    }
                    else if (Constant.USER_STATUS_RESET.equals(user.getStatus())
                            || Constant.USER_STATUS_RESET_AND_LOCK_BY_OTP.equals(user.getStatus())
                            || Constant.YES.equals(user.getIsForceChangePwd())) {
                        custInfo.setState(Constant.MB_CUSTOMER_STATE_IS_CHANGE_PWD);
                    }
                    else {
                        custInfo.setState(Constant.MB_CUSTOMER_STATE_ACTIVED);
                    }
                    cust.setPhoneNo(cust.getPhoneNo().trim());
                    if (!Utility.isNull(cust.getNm())) {
                        cust.setNm(cust.getNm().trim());
                    }

                    CustomerInfo customerInfo = CustomerInfo.builder()
                            .custName(custInfo.getNm())
                            .idCardNumber(custInfo.getIdTypNo())
                            .idCardType(custInfo.getIdTypType())
                            .phoneNumber(custInfo.getPhoneNo())
                            .userName(custInfo.getUserId())
                            .build();
                    response.setCustomerInfoInitSdk(customerInfo);

                }else{
//                    result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
                    result = new SimpleResult(ResponseCode.CUSTOMER_NOT_EXISTED.getDesc(), false, ResponseCode.CUSTOMER_NOT_EXISTED.getCode());
                }
            }else{
                result = new SimpleResult(ResponseCode.CUSTOMER_NOT_EXISTED.getDesc(), false, ResponseCode.CUSTOMER_NOT_EXISTED.getCode());
            }


        }catch (Exception e){
            AppLog.error("[SDK Exception Init] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }
        context.setCustomer(custInfo);
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
