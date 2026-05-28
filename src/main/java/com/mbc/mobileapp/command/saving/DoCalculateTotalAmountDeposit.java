package com.mbc.mobileapp.command.saving;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.constant.MBCResponseCode;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;

import com.mbc.mobileapp.api.CallMsILService;
import com.mbc.mobileapp.api.model.saving.account.AccountSaving;
import com.mbc.mobileapp.api.model.saving.account.SavingAccountListInput;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import com.mbc.mobileapp.api.model.saving.interest.InterestRate;

import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.interest.InterestRequest;

import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoCalculateTotalAmountDeposit implements Command {

    private final CallMsILService callMsILService;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        InterestRequest reqData = request.getInterestRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        //fixed : monthly + maturity
        List<InterestOutput> interestOutputV2 = response.getInterestResponseV2();
        //flexi || real_time
        InterestOutput interestOutputV1 = response.getInterestOutput();

        List<InterestOutput> interestOutputs = Objects.nonNull(interestOutputV1) ? Arrays.asList(interestOutputV1) : interestOutputV2;

        try {
            //calculateTotalAmtDeposit - so tien đa mo saving quy đoi theo USD và KHR
            //calculateTotalAmtDeposit = amtFixed + amtFlexi + amtRealTime
            if (SavingDepositConstant.T24KycStatus.PARTIAL.equalsIgnoreCase(customer.getKycStatus())) {

                SavingAccountListInput savingAccountListInput = SavingAccountListInput.builder()
                        .customerId(customer.getHostCifId())
                        .build();

                ExecuteT24Output<List<AccountSaving>> savingAccV3Output =
                        callMsILService.getSavingAccountListV3(savingAccountListInput,
                        customer.getId(), request.getRequestId());

                if (Objects.isNull(savingAccV3Output)) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(savingAccV3Output.getStatus())) {
                    result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                            MBCResponseCode.INTEREST_RATE_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                //Customer doesn't have saving accounts on T24
                if (CollectionUtils.isEmpty(savingAccV3Output.getData())) {
                    context.setResponse(response);
                    return !result.isOk();
                }
                List<InterestOutput> interestWithTotalAmount = interestOutputs.stream().map(io -> {
                    if (!CollectionUtils.isEmpty(io.getInterestRate()))
                        io.setInterestRate(this.calculateTotalAmtDeposit(io.getInterestRate(), savingAccV3Output.getData()));
                    return io;
                }).collect(Collectors.toList());

                response.setInterestResponseV2(interestWithTotalAmount);
                //case flexi || real_time has only one element
                if (Objects.nonNull(interestOutputV1))
                    response.setInterestOutput(interestWithTotalAmount.get(0));
            }

            context.setResponse(response);
        } catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }

    private List<InterestRate> calculateTotalAmtDeposit(List<InterestRate> interestRate, List<AccountSaving> savingAccV3Output) {
        SavingExchangeRate savingExchangeRate = new SavingExchangeRate();
        BigDecimal totalAmtUsd = BigDecimal.valueOf(savingAccV3Output.stream()
                .filter(saving -> Constant.CURRENCY_TYPE_USD.equalsIgnoreCase(saving.getAccountCurrency())
                        && StringUtils.isNotEmpty(saving.getBalance().getWorking()))
                .map(saving -> Double.valueOf(saving.getBalance().getWorking()))
                .reduce(0.000, Double::sum)).setScale(2,
                RoundingMode.HALF_UP);;

        BigDecimal totalAmtKhr = BigDecimal.valueOf(savingAccV3Output.stream()
                .filter(saving -> Constant.CURRENCY_TYPE_KHR.equalsIgnoreCase(saving.getAccountCurrency())
                        && StringUtils.isNotEmpty(saving.getBalance().getWorking()))
                .map(saving -> Double.valueOf(saving.getBalance().getWorking()))
                .reduce(0.000, Double::sum)).setScale(2,
                RoundingMode.HALF_UP);;

        return interestRate.stream().map(rate -> {
            BigDecimal amountExchangeUsd = savingExchangeRate.exchangeAmountToTargetCcy(totalAmtUsd, Constant.CURRENCY_TYPE_USD, rate.getCurrency());
            BigDecimal amountExchangeKhr = savingExchangeRate.exchangeAmountToTargetCcy(totalAmtKhr, Constant.CURRENCY_TYPE_KHR, rate.getCurrency());
            rate.setTotalAmountDeposit(amountExchangeUsd.add(amountExchangeKhr));
            return rate;
        }).collect(Collectors.toList());
    }

}
