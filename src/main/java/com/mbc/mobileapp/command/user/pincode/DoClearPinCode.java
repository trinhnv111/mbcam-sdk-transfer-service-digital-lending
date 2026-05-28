package com.mbc.mobileapp.command.user.pincode;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DoClearPinCode implements Command {

    @Autowired
    private CustRepo custRepo;

    @Autowired
    private ImIeUserRepo imIeUserRepo;

    private String PATTERN_REGEX = "^(?!(.)\\1{5})(?!012345|123456|234567|345678|456789|567890|098765|987654|876543|765432|654321|543210)\\d{6}$"; //"^[0-9]+$";

    @Value("${environment}")
    private String env;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        CustInfo custInfo = processContext.getCustomer();


        try {

            Cust cust = custRepo.findByIdTypNoAndIsDelete(request.getIdTypNo(), Constant.NO);
            if(Objects.nonNull(cust)){

                ImIeUser imIeUser = imIeUserRepo.findByCd(cust.getSpiUsrCd());

                if (!Utility.isNull(imIeUser.getPinCode())){

                    imIeUser.setPinCode(null);

                    imIeUserRepo.saveAndFlush(imIeUser);
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
