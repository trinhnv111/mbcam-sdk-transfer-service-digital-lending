package com.mbc.mobileapp.command.user.initsdk;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.ComTokenInitSdk;
import com.mbc.common.object.CustInfo;
import com.mbc.common.object.TokenValidate;
import com.mbc.common.repository.ComPartnerUserRepo;
import com.mbc.common.repository.ComTokenInitSdkRepo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.TokenUtil;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.user.initsdk.InitSdkInfo;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoValidateToken implements Command {

    @Value("${environment}")
    private String environment;

    @Autowired
    private ComPartnerUserRepo comPartnerUserRepo;

    @Autowired
    private ComTokenInitSdkRepo comTokenInitSdkRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        // context.getResponse();
        CustInfo custInfo = null;
        InitSdkInfo initSdkInfo = request.getInitSdkInfo();

        try {
            TokenUtil tokenUtil = new TokenUtil();
            StringBuilder pathFile = new StringBuilder("rsakey/");
            pathFile.append(environment);
            pathFile.append("/public_key.pem");

            TokenValidate tokenValidate = tokenUtil.validateToken(initSdkInfo.getToken(), tokenUtil.getPublicKey(pathFile.toString()));
            if("000".equals(tokenValidate.getStatus())){
                String tid = tokenValidate.getClaims().get("tid", String.class);
                String idCard = tokenValidate.getClaims().get("idCard", String.class);
                String phoneNumber = tokenValidate.getClaims().get("phoneNumber", String.class);

                ComTokenInitSdk comTokenInitSdk = comTokenInitSdkRepo.findById(tid).get();
                if(Objects.isNull(comTokenInitSdk.getIsVerify())
                    && Objects.isNull(comTokenInitSdk.getStatus())){

                    //fix lỗi pentest
                    if(Objects.nonNull(idCard) && !idCard.equals(initSdkInfo.getIdCardNumber())){
                        result = new SimpleResult(MBCResponseCode.TOKEN_INIT_SDK_INVALID.getDesc(), false,
                                MBCResponseCode.TOKEN_INIT_SDK_INVALID.getCode());
                    }else{
                        comTokenInitSdk.setIsVerify(Constant.STATUS_SUCCESS);
                        comTokenInitSdk.setStatus(tokenValidate.getStatus());
                        comTokenInitSdk.setIdCard(initSdkInfo.getIdCardNumber());
                        comTokenInitSdkRepo.saveAndFlush(comTokenInitSdk);
                        response.setTid(tid);

                    }

                }else{
                    result = new SimpleResult(MBCResponseCode.TOKEN_INIT_SDK_INVALID.getDesc(), false,
                            MBCResponseCode.TOKEN_INIT_SDK_INVALID.getCode());
                }
            }else{
                result = new SimpleResult(tokenValidate.getDesc(), false,
                        tokenValidate.getStatus());
            }

        }catch (Exception e){
            AppLog.error("[Exception Validate Token] requestId: " + request.getRequestId()+ " desc: " , e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
