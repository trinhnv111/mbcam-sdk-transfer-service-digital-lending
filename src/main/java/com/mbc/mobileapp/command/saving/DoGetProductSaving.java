package com.mbc.mobileapp.command.saving;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.dto.ProductSavingDto;
import com.mbc.mobileapp.object.ProductSavingOption;
import com.mbc.mobileapp.object.ProductSavingV2;
import com.mbc.mobileapp.repository.ComProductSavingRepoExtd;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoGetProductSaving implements Command {

    private final ComProductSavingRepoExtd comProductSavingRepo;

    @Override
    public boolean execute(Context cntxt) throws Exception {

        ProcessContext context = (ProcessContext) cntxt;
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        Validator.Result result = Validator.Result.OK;

        try {
            List<ProductSavingDto> lstProductSaving = comProductSavingRepo.findActiveProduct();
            Set<String> savingProduct = new HashSet<>();
            List<ProductSavingV2> productSaving = lstProductSaving.stream().filter(ps -> savingProduct.add(ps.getId()))
                    .map(ps -> ProductSavingV2.builder()
                            .id(ps.getId())
                            .name(ps.getName())
                            .nameKh(ps.getNameKh())
                            .options(lstProductSaving.stream().filter(e -> e.getGroupId().equals(ps.getGroupId()))
                                    .map(op -> ProductSavingOption.builder()
                                            .nameEn(op.getNameEn())
                                            .nameKh(op.getNameKh())
                                            .category(op.getCategory())
                                            .subCategory(op.getSubCategory())
                                            .productCode(op.getProductCode())
                                            .subProduct(op.getSubProduct())
                                            .amtMinDepositUsd(op.getAmtMinDepositUsd())
                                            .amtMinDepositKhr(op.getAmtMinDepositKhr())
                                            .amtMinTopupUsd(op.getAmtMinTopupUsd())
                                            .amtMinTopupKhr(op.getAmtMinTopupKhr())
                                            .amtMaxNotFullKycUsd(op.getAmtMaxNotFullKycUsd())
                                            .amtMaxNotFullKycKhr(op.getAmtMaxNotFullKycKhr())
                                            .currencyAvailable(op.getCurrencyAvailable())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build()).collect(Collectors.toList());
            response.setLstProductSavingV2(productSaving);
        } catch (Exception e) {
            AppLog.error("[Exception Get Product Saving] requestId: "+request.getRequestId()+" desc: ", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }

        context.setResponse(response);
        context.setResult(result);
        return !result.isOk();
    }

}
