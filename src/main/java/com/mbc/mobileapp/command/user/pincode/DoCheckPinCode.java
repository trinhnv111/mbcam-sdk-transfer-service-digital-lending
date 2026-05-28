package com.mbc.mobileapp.command.user.pincode;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.Cust;
import com.mbc.common.entity.ImIeUser;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.CustRepo;
import com.mbc.common.repository.ImIeUserRepo;
import com.mbc.common.util.*;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoCheckPinCode implements Command {

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ImIeUserRepo imIeUserRepo;


    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();


        try {
            Cust cust = custRepo.findByIdTypNoAndIsDelete(request.getIdTypNo(), Constant.NO);
            if(Objects.nonNull(cust)){
                if(Constant.NO.equals(cust.getIsInactive()) && !Utility.isNull(cust.getUserId())){
                    ImIeUser imIeUser = imIeUserRepo.findByCd(cust.getSpiUsrCd());
                    if(Utility.isNull(imIeUser.getPinCode())){
                        result = new SimpleResult(MBCResponseCode.PINCODE_AUTH_IS_NULL.getDesc(), false, MBCResponseCode.PINCODE_AUTH_IS_NULL.getCode());
                    }
                }else {
                    result = new SimpleResult(ResponseCode.CUSTOMER_INACTIVE.getDesc(), false, ResponseCode.CUSTOMER_INACTIVE.getCode());
                }
            }else{
                result = new SimpleResult(ResponseCode.CUSTOMER_NOT_EXISTED.getDesc(), false, ResponseCode.CUSTOMER_NOT_EXISTED.getCode());
            }

        }catch (Exception e){
            AppLog.error("[SDK Exception Set Pin Code] requestId: "+request.getRequestId()+" desc: " , e);
            result = new SimpleResult(ResponseCode.COMMON_FAIL.getDesc(), false, ResponseCode.COMMON_FAIL.getCode());
        }
        processContext.setResult(result);
        return !result.isOk();

    }
}
