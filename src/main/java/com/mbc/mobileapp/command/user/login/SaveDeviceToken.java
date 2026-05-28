/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 5:36:04 PM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-11-03 5:36:04 PM 
 *  tagId    : mbcam-mobileapp-user
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.command.user.login;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComMobileDevice;
import com.mbc.common.entity.ComMobileUtility;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComMobileDeviceRepo;
import com.mbc.common.repository.ComMobileUtilityRepo;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author danlv.os
 *
 */
@Service
public class SaveDeviceToken implements Command {

    @Autowired
    private ComMobileDeviceRepo comMobileDeviceRepo;

    @Autowired
    private ComMobileUtilityRepo comMobileUtilityRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo cust = context.getCustomer();

        ComMobileDevice mobileDeviceTemp = new ComMobileDevice();
        mobileDeviceTemp.setDeviceId(request.getDeviceId());
        mobileDeviceTemp.setCreatedBy(request.getUserId());
        mobileDeviceTemp.setPhoneId(request.getPhoneId());
        mobileDeviceTemp.setDeviceToken(request.getDeviceToken());
        mobileDeviceTemp.setNm(StringUtils.upperCase(request.getUserId()));
        mobileDeviceTemp.setPhoneId(request.getPhoneId());
        mobileDeviceTemp.setDscp(Constant.DEVICE_ACTION_ACTIVE);
        mobileDeviceTemp.setStatusLogin(Constant.STATUS_1);
        mobileDeviceTemp.setStatus(Constant.STATUS_1);
        mobileDeviceTemp.setIsNotify(Constant.YES);
        mobileDeviceTemp.setCustId(context.getCustomer().getId());

        List<ComMobileDevice> mobileDeviceList =
            comMobileDeviceRepo.findByDeviceIdAndStatus(mobileDeviceTemp.getDeviceId(), mobileDeviceTemp.getStatus());
        // nếu device chưa có trong DB thi them moi ban ghi
        if (mobileDeviceList == null || mobileDeviceList.isEmpty()) {
            result =
                new SimpleResult(ResponseCode.DEVICE_INACTIVE.getDesc(), true, ResponseCode.DEVICE_INACTIVE.getCode());
            context.setResult(result);

            comMobileDeviceRepo.save(mobileDeviceTemp);
            saveActiveDevice(mobileDeviceTemp.getDeviceId(), cust.getId(), request.getDigitalChannel(), null);

            return !result.isOk();
        }
        else {
            // neu co roi thi unlink cac device cu, roi insert device moi

            for (ComMobileDevice mobileDevice : mobileDeviceList) {
                if (StringUtils.isBlank(mobileDevice.getDeviceToken())) {
                    mobileDevice.setDeviceToken(request.getDeviceToken());
                }
            }
            comMobileDeviceRepo.saveAll(mobileDeviceList);
        }

        return !result.isOk();
    }


    public void saveActiveDevice(String deviceId, String custId, String channel, String fingerPrint) throws Exception {
        ComMobileUtility comMobileUtility = comMobileUtilityRepo.findByDeviceId(deviceId);
        if (comMobileUtility == null) {
            comMobileUtility = new ComMobileUtility();
            comMobileUtility.setCustId(custId);
            comMobileUtility.setDeviceId(deviceId);
            comMobileUtility.setCreatedDate(new Date());
            comMobileUtility.setCreatedBy(channel);
            comMobileUtility.setFingerPrint(fingerPrint);
            comMobileUtilityRepo.saveAndFlush(comMobileUtility);
        }
        else {
            comMobileUtility.setCustId(custId);
            comMobileUtility.setCreatedBy(channel);
            comMobileUtility.setCreatedDate(new Date());
            comMobileUtility.setFingerPrint(fingerPrint);
            comMobileUtilityRepo.saveAndFlush(comMobileUtility);
        }

    }

}
