//package com.mbc.mobileapp.command.digital_loan.salary_advance;
//
//import com.mbc.common.bean.ProcessContext;
//import com.mbc.common.bean.ResponseCode;
//import com.mbc.common.object.CustInfo;
//import com.mbc.common.util.JSON;
//import com.mbc.common.validator.base.Validator;
//import com.mbc.gateway.validator.result.SimpleResult;
//import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.chain.Command;
//import org.apache.commons.chain.Context;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class DoSalaryAdvanceInit implements Command {
//
//    @Override
//    public boolean execute(Context context) throws Exception {
//        ProcessContext processContext = (ProcessContext) context;
//        Validator.Result result = Validator.Result.OK;
//        CustInfo custInfo = processContext.getCustomer();
//        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
//        try {
//            /*TODO
//                - Call eMoney get cust infor (note chưa có api eMoney)
//                - pre-check SDK BE: match national id number, date of birth >= 18, >= 6 months continuous salary payment.
//                - Check AML
//                - Get customer info ( customer inquiry từ ms customer) lấy thông tin phone number, current address
//                - Save bản ghi tạm vào COM_LOAN_DISBUR_LMT (bao gồm thông tin lương) với step là  CHECK_CUST
//                - Response lại cho FE ( bao gồm id bản ghi tạm)
//             */
//        } catch (Exception e) {
//            log.error("[Exception Salary Advance Init] requestId: {} desc: {}", request.getRequestId(), JSON.stringify(e));
//            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getCode(), false, ResponseCode.TRANSACTION_FAIL.getDesc());
//        }
//        processContext.setResult(result);
//        return !result.isOk();
//    }
//}
