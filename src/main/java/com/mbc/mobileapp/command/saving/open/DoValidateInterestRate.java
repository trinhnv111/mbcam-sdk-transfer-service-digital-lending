
package com.mbc.mobileapp.command.saving.open;

import com.mbc.common.api.ApiCustomer;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.services.il.customerinfo.CustomerInfoInput;
import com.mbc.common.services.il.customerinfo.CustomerInfoT24;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;

import com.mbc.mobileapp.api.CallSavingService;
import com.mbc.mobileapp.api.model.saving.interest.InterestInput;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import com.mbc.mobileapp.api.model.saving.interest.InterestRate;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.saving.open.SavingInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DoValidateInterestRate implements Command {

    private final CallSavingService callSavingService;

    private final ApiCustomer apiCustomer;

    @Override
    public boolean execute(Context cntxt) throws Exception {
        ProcessContext context = (ProcessContext) cntxt;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CustInfo customer = context.getCustomer();
        SavingInfo savingFixedDepositInfo = request.getSavingInfo();
        try {

            CustomerInfoInput customerInfoInput = new CustomerInfoInput();
            customerInfoInput.setCustomerId(customer.getHostCifId());
            ExecuteT24Output<CustomerInfoT24> cusInfo_Output =
                apiCustomer.getCustomerInfo(customerInfoInput, customer.getId(), request.getRequestId());

            if (Objects.isNull(cusInfo_Output)) {
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                    ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }
            if (Constant.CALL_MICROSERVICE_SUCCESS.equals(cusInfo_Output.getStatus())) {
                InterestInput input = new InterestInput();
                input.setProductCode(request.getSavingProductCode());
                input.setResident("KH".equals(cusInfo_Output.getData().getPerson().getResidence()) ? "Y" : "N");

                ExecuteT24Output<InterestOutput> output =
                    callSavingService.getInterestRate(input, customer.getId(), request.getRequestId());
                if (Objects.isNull(output)) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus()) && output.getData() != null
                    && !CollectionUtils.isEmpty(output.getData().getInterestRate())) {

                    // get tax rate
                    savingFixedDepositInfo.setTaxRate(output.getData().getTaxRate().toString());

                    
                    // validate interest rate with t24 interest
                    boolean isRateExist = output.getData().getInterestRate().stream()
                        .filter(interestRate -> interestRate.getCurrency()
                            .equalsIgnoreCase(savingFixedDepositInfo.getCurrency()))
                        .map(InterestRate::getRestPeriodList).filter(Objects::nonNull).flatMap(Collection::stream)
                        .filter(
                            rest -> rest.getBidRate().compareTo(new BigDecimal(savingFixedDepositInfo.getInterestRate())) == 0
                                && rest.getRestPeriod().equals(savingFixedDepositInfo.getTerm()))
                        .findAny().isPresent();
                    
                    if (!isRateExist) {
                        result = new SimpleResult(MBCResponseCode.INTEREST_RATE_NOT_VALID.getDesc(), false,
                            MBCResponseCode.INTEREST_RATE_NOT_VALID.getCode());
                    }
                }
                else {
                    result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                        MBCResponseCode.INTEREST_RATE_FAIL.getCode());
                }
            }
            else {
                result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                    MBCResponseCode.INTEREST_RATE_FAIL.getCode());
            }

        }
        catch (Exception e) {
            AppLog.error("[SDK Exception Validate Open Saving] requestId: "+request.getRequestId()+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }
}
