
package com.mbc.mobileapp.command.user.login;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.datacache.DataCachedCollection;
import com.mbc.common.entity.ImIeUser;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComMobileUtilityRepo;
import com.mbc.common.repository.CustCustomRepo;
import com.mbc.common.repository.ImIeUserRepo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.object.ValidInputIncorrect;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@SuppressWarnings("rawtypes")
public class CheckCustomerPassword implements Command {

    private final static ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CustCustomRepo custCustomRepo;

    @Autowired
    private ComMobileUtilityRepo mobileUtilityRepository;

    @Autowired
    private ImIeUserRepo imIeUserRepository;

    @Autowired
    private DataCachedCollection dataCachedCollection;

    // @Autowired
    // private ComDeviceLoginHistRepo deviceLoginHistRepository;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        // CommonServiceResponse response = (CommonServiceResponse)
        // context.getResponse();
        CustInfo cust = context.getCustomer();

        String userId = request.getUserId();

        String pinCode = request.getPinCode();
        String deviceIdCommon = request.getDeviceIdCommon();
        String digitalChannel = request.getDigitalChannel();

        try {
            if (StringUtils.isNotBlank(userId) && cust == null) {
//                cust = custCustomRepo.getCustByUserId(userId, digitalChannel);
                cust = custCustomRepo.getCustByUserIdForSdk(userId, digitalChannel);
            }
            if (cust == null) {
                result = new SimpleResult(ResponseCode.USER_INCORRECT.getDesc(), false,
                        ResponseCode.USER_INCORRECT.getCode());
                String data = RedisServer.getCacheRedis(request.getDeviceIdCommon() + Constant.HYPHEN + "GW006");
                if (Utility.isNull(data)) {
                    ValidInputIncorrect inputIncorrect = new ValidInputIncorrect();
                    inputIncorrect.setCode(ResponseCode.USER_INCORRECT.getCode());
                    inputIncorrect.setCount(1);
                    inputIncorrect.setDateTime(new Date());

                    data = mapper.writeValueAsString(inputIncorrect);
                    RedisServer.saveCacheRedis(request.getDeviceIdCommon() + Constant.HYPHEN + "GW006", data, 15);
                } else {
                    ValidInputIncorrect inputIncorrect = mapper.readValue(data, ValidInputIncorrect.class);
                    int minuteDiff = DateCompareUtil.minutesDiff(inputIncorrect.getDateTime(), new Date());
                    if (inputIncorrect.getCount() >= 5 && minuteDiff >= 10) {
                        inputIncorrect.setCount(1);
                    } else {
                        inputIncorrect.setCount(inputIncorrect.getCount() + 1);
                    }
                    inputIncorrect.setDateTime(new Date());
                    data = mapper.writeValueAsString(inputIncorrect);
                    RedisServer.saveCacheRedis(request.getDeviceIdCommon() + Constant.HYPHEN + "GW006", data, 15);
                }
            } else {
                ImIeUser user = cust.getImIeUser();
//                if (Constant.USER_STATUS_LOCKED.equalsIgnoreCase(user.getStatus())) {
//                    result = new SimpleResult(ResponseCode.CUSTOMER_AUTO_LOCKED.getDesc(), false,
//                        ResponseCode.CUSTOMER_AUTO_LOCKED.getCode());
//                }
//                else {
                context.putVar("user", user);
//                    if (password.length() < 32) {
//                        password = MD5.MD5Password(password);
//                    }
                boolean isCorrectPassword = Utility.checkPassword(pinCode, user.getPinCode());

                if (!isCorrectPassword) {

                    Date currentDate = new Date();
                    Date resetDate = user.getLatestActnDt();
                    int minutes = DateCompareUtil.minutesDiff(resetDate, currentDate);
                    int time_lock = Integer.valueOf(dataCachedCollection
                            .getSysParam(Constant.SystemParamMap.TIME_LOCK_PASS_INCORRECT.getParam()).getValue());

                    if (Constant.USER_STATUS_AUTO_LOCKED.equals(user.getStatus()) && minutes > time_lock) {
                        imIeUserRepository.updateStatusUser(cust.getSpiUsrCd(), cust.getUserId(), 1,
                                Constant.USER_STATUS_ACTIVE);

                        result = new SimpleResult(ResponseCode.PASSWORD_INCORRECT.getDesc(), false,
                                ResponseCode.PASSWORD_INCORRECT.getCode());
                    } else {
                        int loginCount = user.getLoginCount().intValue();
                        loginCount += 1;
                        if (loginCount < 5) {

                            imIeUserRepository.updateStatusUser(cust.getSpiUsrCd(), cust.getUserId(), loginCount,
                                    user.getStatus());

                            result = new SimpleResult(ResponseCode.PASSWORD_INCORRECT.getDesc(), false,
                                    ResponseCode.PASSWORD_INCORRECT.getCode());
                        } else {

                            imIeUserRepository.updateStatusUser(cust.getSpiUsrCd(), cust.getUserId(), loginCount,
                                    Constant.USER_STATUS_AUTO_LOCKED);

                            result = new SimpleResult(ResponseCode.CUSTOMER_AUTO_LOCKED.getDesc(), false,
                                    ResponseCode.CUSTOMER_AUTO_LOCKED.getCode());
                        }
                    }
                } else {
                    if (Constant.USER_STATUS_RESET.equals(user.getStatus())) {
                        Date currentDate = new Date();
                        Date resetDate = user.getPasswordchangedate();
                        int hours = DateCompareUtil.hoursDiff(resetDate, currentDate);
                        if (hours > Constant.TIME_LIFE_FOGOT_PASSWORD_HOURS) {
                            result = new SimpleResult(ResponseCode.PASSWORD_RESET_EXPIRE.getDesc(), false,
                                    ResponseCode.PASSWORD_RESET_EXPIRE.getCode());
                        } else {
                            imIeUserRepository.updateStatusUser(cust.getSpiUsrCd(), cust.getUserId(), 0,
                                    user.getStatus());
                            if ("OFF".equals(request.getFingerPrint())) {

                                mobileUtilityRepository.registerFingerprint(deviceIdCommon, cust.getId(), "OFF");
                            }
                        }
                    } else if (Constant.USER_STATUS_AUTO_LOCKED.equals(user.getStatus())) {
                        Date currentDate = new Date();
                        Date resetDate = user.getLatestActnDt();
                        int minutes = DateCompareUtil.minutesDiff(resetDate, currentDate);
                        int time_lock = Integer.valueOf(dataCachedCollection
                                .getSysParam(Constant.SystemParamMap.TIME_LOCK_PASS_INCORRECT.getParam()).getValue());
                        if (minutes > time_lock) {
                            imIeUserRepository.updateStatusUser(cust.getSpiUsrCd(), cust.getUserId(), 0,
                                    Constant.USER_STATUS_ACTIVE);
                            cust.setState(Constant.MB_CUSTOMER_STATE_ACTIVED);
                            cust.setImUserStatus(Constant.USER_STATUS_ACTIVE);

                            if ("OFF".equals(request.getFingerPrint())) {
                                mobileUtilityRepository.registerFingerprint(deviceIdCommon, cust.getId(), "OFF");
                            }
                        } else {
                            result = new SimpleResult(ResponseCode.CUSTOMER_LOCKED.getDesc(), false,
                                    ResponseCode.CUSTOMER_LOCKED.getCode());
                        }

                    } else {
                        imIeUserRepository.updateStatusUser(cust.getSpiUsrCd(), cust.getUserId(), 0,
                                user.getStatus());
                        if ("OFF".equals(request.getFingerPrint())) {
                            mobileUtilityRepository.registerFingerprint(deviceIdCommon, cust.getId(), "OFF");
                        }
                    }
                }
            }

        } catch (Exception e) {
            result = new SimpleResult(ResponseCode.PASSWORD_VALIDATE_FAIL.getDesc(), false,
                    ResponseCode.PASSWORD_VALIDATE_FAIL.getCode());
            // result = new SimpleResult(e.toString().length() > 500 ? e.toString().substring(0, 495) : e.toString(),
            // false,
            // ResponseCode.PASSWORD_VALIDATE_FAIL.getCode());
            AppLog.error("[Exception Validate Password] requestId: "+request.getRequestId()+ " desc: ", e);
        }

        context.setCustomer(cust);
        context.setResult(result);
        return !result.isOk();
    }

}
