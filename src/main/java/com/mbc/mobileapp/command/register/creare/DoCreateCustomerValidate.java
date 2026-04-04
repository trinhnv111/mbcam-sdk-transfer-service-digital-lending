package com.mbc.mobileapp.command.register.creare;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.entity.Cust;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.Utility;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.object.resgister.RegisterCustInfo;
import com.mbc.mobileapp.repository.CustRepoExtend;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DoCreateCustomerValidate implements Command {

    @Autowired
    private CustRepoExtend custExtendRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;
        RegisterCustInfo openCustomerInfo = request.getRegisterCustInfo();

        // TokenOtp otp = (TokenOtp) context.getVar(Constant.KeyVar.OTP);

        try {
            // if (otp == null) {
            // result =
            // new SimpleResult(ResponseCode.OTP_IS_NULL.getDesc(), false, ResponseCode.OTP_IS_NULL.getCode());
            // }
            // else
            // if (!openCustomerInfo.getPhoneNumber().equals(otp.getUserId())) {
            // result = new SimpleResult(MBCResponseCode.PHONE_NO_REGISTER_ACCT_AND_OTP_MISMATCHED.getDesc(), false,
            // MBCResponseCode.PHONE_NO_REGISTER_ACCT_AND_OTP_MISMATCHED.getErrorCode());
            // }
            // else

            long countCustByUsername = custExtendRepo.countByUserId(openCustomerInfo.getPhoneNumber());
            // custRepo.countByIsInactiveAndIsDeleteAndUserIdIgnoreCase(Constant.NO, Constant.NO,
            // request.getUsername());

            if (countCustByUsername > 0) {
                result =
                    new SimpleResult(request.getUsername() + " account has been used. Please enter another account",
                        false, MBCResponseCode.ACCOUNT_EXITSTED.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            if (!Utility.isNull(openCustomerInfo.getEmail())) {
//                long countCustByMail = custRepo.countByIsInactiveAndIsDeleteAndCorrespondentEmailIgnoreCase(Constant.NO,
//                    Constant.NO, request.getEmail());
                
                List<Cust> custEmail = custExtendRepo.findByCorrespondentEmailIgnoreCase(openCustomerInfo.getEmail());

                if (custEmail.size() > 0) {
                    for (Cust cust : custEmail) {
                        if(Constant.NO.equals(cust.getIsDelete())) {
                            result = new SimpleResult(
                                "The email address " + openCustomerInfo.getEmail()
                                    + " is already in use. Please enter another email address",
                                false, MBCResponseCode.EMAIL_EXITSTED.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }
                    }
                }
            }

            if (Objects.nonNull(openCustomerInfo.getRmInfo())
                    && !Utility.isNull(openCustomerInfo.getRmInfo().getRmCode())
                && !Utility.isFullNameValid(openCustomerInfo.getRmInfo().getRmCode())) {
                result =
                    new SimpleResult(ResponseCode.DATA_INVALID.getDesc(), false, ResponseCode.DATA_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            
            if (Objects.nonNull(openCustomerInfo.getRmInfo())
                    && !Utility.isNull(openCustomerInfo.getRmInfo().getRmName())
                && !Utility.isFullNameValid(openCustomerInfo.getRmInfo().getRmName())) {
                result =
                    new SimpleResult(ResponseCode.DATA_INVALID.getDesc(), false, ResponseCode.DATA_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            
            if (CommonServiceConstant.ID_CARD_DOC_NAME_T24_PASSPORT.equals(openCustomerInfo.getIdCardType())
                && Utility.isNull(openCustomerInfo.getNationalId())) {
                result =
                    new SimpleResult(ResponseCode.DATA_INVALID.getDesc(), false, ResponseCode.DATA_INVALID.getCode());
                context.setResult(result);
                return !result.isOk();
            }
        }
        catch (Exception e) {
            AppLog.error("[SDK Exception Validate Create Customer] requestId: " + request.getRequestId() + " desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

}
