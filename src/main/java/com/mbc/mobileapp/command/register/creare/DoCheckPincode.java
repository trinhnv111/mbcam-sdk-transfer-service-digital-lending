package com.mbc.mobileapp.command.register.creare;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DoCheckPincode implements Command {

    private String PATTERN_REGEX = "^(?!(.)\\1{5})(?!012345|123456|234567|345678|456789|567890|098765|987654|876543|765432|654321|543210)\\d{6}$"; //"^[0-9]+$";

    @Value("${environment}")
    private String env;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();

        RegisterCustInfo openCustomerInfo = request.getRegisterCustInfo();

        try{
            if (!Utility.isNull(openCustomerInfo.getPincode())){
                TokenUtil rsa = new TokenUtil();
                StringBuilder pathFile = new StringBuilder();
                pathFile.append("rsakey");
                pathFile.append("/");
                pathFile.append(env);
                pathFile.append("/");
                pathFile.append("private_key.pem");

                String pincode = rsa.decrypt(rsa.getPrivateKey(pathFile.toString()), openCustomerInfo.getPincode().getBytes());

                if (!pincode.matches(PATTERN_REGEX)) {
                    result = new SimpleResult(MBCResponseCode.PINCODE_AUTH_INVALID.getDesc(), false,
                            MBCResponseCode.PINCODE_AUTH_INVALID.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                if (pincode.length() != 6) {
                    result = new SimpleResult(MBCResponseCode.PINCODE_AUTH_INVALID.getDesc(), false,
                            MBCResponseCode.PINCODE_AUTH_INVALID.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                request.setPinCode(pincode);
//                String hashPinCode = BCrypt.hashpw(MD5.MD5Password(pincode), BCrypt.gensalt(6));
//                imIeUser.setPinCode(hashPinCode);
//
//                imIeUserRepo.saveAndFlush(imIeUser);
            }else{
                result = new SimpleResult(MBCResponseCode.PINCODE_IS_EXIST.getDesc(), false, MBCResponseCode.PINCODE_IS_EXIST.getCode());
            }

        }catch (Exception e){
            AppLog.error("[SDK Exception Check Pin Code] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }

        context.setRequest(request);
        context.setResult(result);
        return !result.isOk();
    }
}
