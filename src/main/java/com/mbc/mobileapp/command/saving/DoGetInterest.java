package com.mbc.mobileapp.command.saving;

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
import com.mbc.mobileapp.api.model.saving.interest.Period;

import com.mbc.mobileapp.constant.SavingDepositConstant;
import com.mbc.mobileapp.dto.ProductSavingDto;
import com.mbc.mobileapp.repository.ComProductSavingRepoExtd;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.rest.saving.interest.InterestRequest;
import com.mbc.mobileapp.rest.saving.interest.InterestResponseV2;

import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoGetInterest implements Command {

    private final CallSavingService callSavingService;

    private final ApiCustomer apiCustomer;

    private final ComProductSavingRepoExtd comProductSavingRepository;

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;

        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        InterestRequest reqData = request.getInterestRequest();
        CustInfo customer = context.getCustomer();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        InterestResponseV2 interestResponse = new InterestResponseV2();
        try {
            CustomerInfoInput customerInfoInput = new CustomerInfoInput();
            customerInfoInput.setCustomerId(customer.getHostCifId());

            ExecuteT24Output<CustomerInfoT24> cusInfo_Output = apiCustomer.getCustomerInfo(customerInfoInput, customer.getId(), request.getRequestId());
            if (Objects.isNull(cusInfo_Output)) {
                result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                        ResponseCode.REQUEST_TIMEOUT.getCode());
                context.setResult(result);
                return !result.isOk();
            }

            //get config product saving
            List<ProductSavingDto> productSaving = comProductSavingRepository.findByGroupId(reqData.getSrvcCd());

            for (ProductSavingDto pv : productSaving) {
                if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(cusInfo_Output.getStatus())) {
                    result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                            MBCResponseCode.INTEREST_RATE_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                ExecuteT24Output<InterestOutput> output = callSavingService.getInterestRate(
                        new InterestInput("KH".equals(cusInfo_Output.getData().getPerson().getResidence()) ? "Y" : "N", pv.getProductCode()),
                        customer.getId(),
                        request.getRequestId());

                if (Objects.isNull(output)) {
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                if (!Constant.CALL_MICROSERVICE_SUCCESS.equals(output.getStatus())) {
                    result = new SimpleResult(MBCResponseCode.INTEREST_RATE_FAIL.getDesc(), false,
                            MBCResponseCode.INTEREST_RATE_FAIL.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }
                InterestOutput interestOutput = InterestOutput.builder()
                        .interestRate(this.mappingInterestInfo(output.getData(), pv, customer.getKycStatus()))
                        .category(output.getData().getCategory())
                        .product(output.getData().getProduct())
                        .taxRate(output.getData().getTaxRate())
                        .currencyAvalable(pv.getCurrencyAvailable())
                        .build();

                interestResponse.getData().add(interestOutput);
            }

            response.setInterestResponseV2(interestResponse.getData());
            context.setResponse(response);

        } catch (Exception e) {
            AppLog.error("ERROR", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResult(result);
        return !result.isOk();
    }

    private List<InterestRate> mappingInterestInfo(InterestOutput interestOutput, ProductSavingDto comProductSaving, String userKycStatus) {
        if (CollectionUtils.isEmpty(interestOutput.getInterestRate()))
            return null;

        return interestOutput.getInterestRate().stream().map(ir -> {
            ir.setInterestRateMax(this.getMaxInterestRate(ir));
            if (SavingDepositConstant.KHR.equalsIgnoreCase(ir.getCurrency())) {

                if (!SavingDepositConstant.T24KycStatus.FULL_KYC.equalsIgnoreCase(userKycStatus)) {
                    ir.setMaxAmountDeposit(comProductSaving.getAmtMaxNotFullKycKhr());
                }
                ir.setMinAmountDeposit(comProductSaving.getAmtMinDepositKhr());
                ir.setMinAmountTopUp(comProductSaving.getAmtMinTopupKhr());
            }

            if (SavingDepositConstant.USD.equalsIgnoreCase(ir.getCurrency())) {
                if (!SavingDepositConstant.T24KycStatus.FULL_KYC.equalsIgnoreCase(userKycStatus)) {
                    ir.setMaxAmountDeposit(comProductSaving.getAmtMaxNotFullKycUsd());
                }
                ir.setMinAmountDeposit(comProductSaving.getAmtMinDepositUsd());
                ir.setMinAmountTopUp(comProductSaving.getAmtMinTopupUsd());
            }
            return ir;
        }).collect(Collectors.toList());
    }

    private BigDecimal getMaxInterestRate(InterestRate interestRate) {
        List<Period> restPeriodList = interestRate.getRestPeriodList();
        if (!CollectionUtils.isEmpty(restPeriodList)) {
            return BigDecimal.valueOf(restPeriodList.stream()
                    .mapToDouble(hits -> hits.getBidRate().doubleValue())
                    .max()
                    .orElse(0.0)).setScale(2);
        }
        return new BigDecimal("0.0");
    }
}
