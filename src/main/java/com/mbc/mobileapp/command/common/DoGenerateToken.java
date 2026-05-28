package com.mbc.mobileapp.command.common;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComPartnerSdk;
import com.mbc.common.entity.ComPartnerUser;
import com.mbc.common.entity.ComTokenInitSdk;
import com.mbc.common.repository.ComPartnerSdkRepo;
import com.mbc.common.repository.ComPartnerUserRepo;
import com.mbc.common.repository.ComTokenInitSdkRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.common.util.RSAUtil;
import com.mbc.common.util.TokenUtil;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.object.TokenData;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DoGenerateToken implements Command {

    @Value("${environment}")
    private String environment;

    @Autowired
    private ComPartnerSdkRepo comPartnerSdkRepo;

    @Autowired
    private ComTokenInitSdkRepo comTokenInitSdkRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        try{
//            List<ComPartnerUser> comPartnerUsers = comPartnerUserRepo.findByStatusAndType(BigDecimal.ONE, "SDK");
//            List<ComPartnerUser> partnerUser = comPartnerUsers.stream().filter(e -> e.getUserId().equals(request.getPartnerSdk())).collect(Collectors.toList());

            ComPartnerSdk comPartnerSdk = comPartnerSdkRepo.findById(request.getPartnerSdk()).get();
            if(Objects.nonNull(comPartnerSdk)
                    && Constant.ACCT_STATUS_ACTIVE.equals(comPartnerSdk.getStatus().name())

            ){
                ComTokenInitSdk comTokenInitSdk = new ComTokenInitSdk();
                comTokenInitSdk.setPartner(comPartnerSdk.getPartnerCode());
                comTokenInitSdk.setIdCard(request.getIdTypNo());
                comTokenInitSdk.setPhoneNumber(request.getPhoneNo());
                comTokenInitSdkRepo.saveAndFlush(comTokenInitSdk);

                TokenData tokenData = TokenData.builder()
                        .partner(comPartnerSdk.getPartnerCode())
                        .tid(comTokenInitSdk.getId())
                        .build();
                TokenUtil tokenUtil = new TokenUtil();
                StringBuilder pathFile = new StringBuilder("rsakey/");
                pathFile.append(environment);
                pathFile.append("/private_key.pem");
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("partner", tokenData.getPartner());
                data.put("auth", tokenData.getAuth());
                data.put("tid", tokenData.getTid());

                String token = tokenUtil.createToken(null, data, tokenUtil.getPrivateKey(pathFile.toString()), 300000);
                response.setToken(token);

                comTokenInitSdk.setToken(token);
                comTokenInitSdkRepo.saveAndFlush(comTokenInitSdk);
            }

        }catch (Exception e){
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }
}
