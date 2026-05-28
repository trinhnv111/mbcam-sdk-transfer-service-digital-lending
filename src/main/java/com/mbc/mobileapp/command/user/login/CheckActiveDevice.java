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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class CheckActiveDevice implements Command {

    @Autowired
    private ComMobileDeviceRepo comMobileDeviceRepo;

    @Autowired
    private ComMobileUtilityRepo comMobileUtilityRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = context.getResult();
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
//            result =
//                new SimpleResult(ResponseCode.DEVICE_INACTIVE.getDesc(), true, ResponseCode.DEVICE_INACTIVE.getCode());
//            context.setResult(result);

            comMobileDeviceRepo.save(mobileDeviceTemp);

            ComMobileUtility comMobileUtility = comMobileUtilityRepo.findByDeviceId(mobileDeviceTemp.getDeviceId());
            if (comMobileUtility == null) {
                comMobileUtility = new ComMobileUtility();
                comMobileUtility.setCustId(cust.getId());
                comMobileUtility.setDeviceId(mobileDeviceTemp.getDeviceId());
                comMobileUtility.setCreatedDate(new Date());
                comMobileUtility.setCreatedBy(request.getDigitalChannel());
                comMobileUtility.setFingerPrint(null);
                comMobileUtilityRepo.saveAndFlush(comMobileUtility);
            }
            else {
                comMobileUtility.setCustId(cust.getId());
                comMobileUtility.setCreatedBy(request.getDigitalChannel());
                comMobileUtility.setCreatedDate(new Date());
                comMobileUtility.setFingerPrint(null);
                comMobileUtilityRepo.saveAndFlush(comMobileUtility);
            }

            return !result.isOk();
        }
        else {
            // neu co roi thi unlink cac device cu, roi insert device moi

            ComMobileDevice activeCurrentCust = null;
            for (int i = 0; i < mobileDeviceList.size(); i++){
                ComMobileDevice mobileDevice = mobileDeviceList.get(i);
                if(cust.getId().equals(mobileDevice.getCustId())){
                    activeCurrentCust = mobileDevice;
                    mobileDeviceList.remove(i);
                }else{
                    mobileDevice.setStatus(Constant.STATUS_0);
                    mobileDevice.setDscp(Constant.DEVICE_ACTION_INACTIVE);
                    mobileDevice.setUpdatedBy(context.getCustomer().getUserId());
                    mobileDeviceList.set(i, mobileDevice);
                }
            }
            comMobileDeviceRepo.saveAllAndFlush(mobileDeviceList);
            if(Objects.isNull(activeCurrentCust)){
                comMobileDeviceRepo.save(mobileDeviceTemp);
            }else{
                activeCurrentCust.setDeviceToken(request.getDeviceToken());
                comMobileDeviceRepo.save(activeCurrentCust);
            }

            ComMobileUtility comMobileUtility = comMobileUtilityRepo.findByDeviceId(mobileDeviceTemp.getDeviceId());
            if (comMobileUtility == null) {
                comMobileUtility = new ComMobileUtility();
                comMobileUtility.setCustId(cust.getId());
                comMobileUtility.setDeviceId(mobileDeviceTemp.getDeviceId());
                comMobileUtility.setCreatedDate(new Date());
                comMobileUtility.setCreatedBy(request.getDigitalChannel());
                comMobileUtility.setFingerPrint(null);
                comMobileUtilityRepo.saveAndFlush(comMobileUtility);
            }
            else {
                comMobileUtility.setCustId(cust.getId());
                comMobileUtility.setCreatedBy(request.getDigitalChannel());
                comMobileUtility.setCreatedDate(new Date());
                comMobileUtility.setFingerPrint(null);
                comMobileUtilityRepo.saveAndFlush(comMobileUtility);
            }

            context.putVar("mobileDeviceDTO", mobileDeviceList);
            context.setResult(result);
        }

        return !result.isOk();
    }
}
