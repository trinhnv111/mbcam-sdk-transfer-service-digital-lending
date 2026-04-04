package com.mbc.mobileapp.command.user.pincode;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComForgotPassHist;
import com.mbc.common.entity.Cust;
import com.mbc.common.entity.ImIeUser;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComForgotPassHistRepo;
import com.mbc.common.repository.ImIeUserRepo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.object.ValidInputIncorrect;
import com.mbc.mobileapp.repository.CustRepoExtend;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service
public class DoForgotPinCode implements Command {

    private final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CustRepoExtend custRepoExtend;

    @Autowired
    private ImIeUserRepo imIeUserRepo;

    @Autowired
    private ComForgotPassHistRepo comForgotPassHistRepo;

    private String PATTERN_REGEX = "^(?!(.)\\1{5})(?!012345|123456|234567|345678|456789|567890|098765|987654|876543|765432|654321|543210)\\d{6}$"; //"^[0-9]+$";

    @Value("${environment}")
    private String env;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) processContext.getResponse();
        CustInfo custInfo = processContext.getCustomer();

        try {
            Cust cust = custRepoExtend.findByNmAndIdTypNoAndIsDeleteAndIsInactive(request.getCustName(), request.getIdTypNo(), Constant.NO, Constant.NO);
            if(Objects.nonNull(cust)){

                ImIeUser user = imIeUserRepo.findByCd(cust.getSpiUsrCd());
                if (Constant.YES.equals(cust.getIsInactive())) {
                    result = new SimpleResult(ResponseCode.CUSTOMER_INACTIVE.getDesc(), false,
                            ResponseCode.CUSTOMER_INACTIVE.getCode());
                }
                else if (Constant.USER_STATUS_LOCKED.equals(user.getStatus())
                        || "LOCK".equals(user.getStatus())) {
                    result = new SimpleResult(ResponseCode.CUSTOMER_LOCKED.getDesc(), false,
                            ResponseCode.CUSTOMER_LOCKED.getCode());

                }else if (Constant.USER_STATUS_AUTO_LOCKED.equals(user.getStatus())) {
                    result = new SimpleResult(ResponseCode.CUSTOMER_AUTO_LOCKED.getDesc(), false,
                            ResponseCode.CUSTOMER_AUTO_LOCKED.getCode());

                }
                else {
//                        ComAuthEkyc comAuthEkyc = comAuthEkycRepo.findByCustId(cust.getId());
//                        if(comAuthEkyc != null) {
//                            response.setBioId(comAuthEkyc.getBioId());
//                            processContext.setResponse(response);
//                        }

                    ComForgotPassHist passHist = new ComForgotPassHist();
                    passHist.setDeviceId(request.getDeviceIdCommon());
                    passHist.setName(cust.getNm().toUpperCase());
                    passHist.setPhoneNumber(cust.getPhoneNo());
                    passHist.setIdCardNumber(cust.getIdTypNo());
                    passHist.setCustId(cust.getId());
                    passHist.setUserId(cust.getUserId());
                    passHist.setCreatedBy(cust.getUserId());
                    passHist.setCreatedDate(new Date());
                    passHist.setDeviceName(request.getDeviceName());
                    passHist.setStatus(Constant.COM_STATUS_INT);
                    passHist.setType("PINCODE");
                    comForgotPassHistRepo.saveAndFlush(passHist);

                    response.setTransId(passHist.getId());
                    processContext.setResponse(response);

                }

            }else {
                result = new SimpleResult(ResponseCode.FORGET_PASSWORD_INFO_INCORRECT.getDesc(), false,
                        ResponseCode.FORGET_PASSWORD_INFO_INCORRECT.getCode());

                String data = RedisServer.getCacheRedis(request.getDeviceIdCommon() + Constant.HYPHEN + "GW050");
                if (Utility.isNull(data)) {
                    ValidInputIncorrect inputIncorrect = new ValidInputIncorrect();
                    inputIncorrect.setCode(ResponseCode.FORGET_PASSWORD_INFO_INCORRECT.getCode());
                    inputIncorrect.setCount(1);
                    inputIncorrect.setDateTime(new Date());

                    data = mapper.writeValueAsString(inputIncorrect);
                    RedisServer.saveCacheRedis(request.getDeviceIdCommon() + Constant.HYPHEN + "GW050", data, 15);
                }else {
                    ValidInputIncorrect inputIncorrect = mapper.readValue(data, ValidInputIncorrect.class);
                    int minuteDiff = DateCompareUtil.minutesDiff(inputIncorrect.getDateTime(), new Date());
                    if(inputIncorrect.getCount() >= 5 && minuteDiff >= 10) {
                        inputIncorrect.setCount(1);
                    }else {
                        inputIncorrect.setCount(inputIncorrect.getCount() + 1);
                    }
                    inputIncorrect.setDateTime(new Date());
                    data = mapper.writeValueAsString(inputIncorrect);
                    RedisServer.saveCacheRedis(request.getDeviceIdCommon() + Constant.HYPHEN + "GW050", data, 15);
                }
            }

        }catch (Exception e){
            AppLog.error("[SDK Exception Forgot Pin Code] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }
        processContext.setResult(result);
        return !result.isOk();

    }
}
