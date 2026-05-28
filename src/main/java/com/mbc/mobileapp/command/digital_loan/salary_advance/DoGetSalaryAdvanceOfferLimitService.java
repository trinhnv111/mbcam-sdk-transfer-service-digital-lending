package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.salary_advance.output.SalaryAdvanceOfferLimitData;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

import static org.aspectj.bridge.Version.SIMPLE_DATE_FORMAT;


@Service
@Slf4j
@RequiredArgsConstructor
public class DoGetSalaryAdvanceOfferLimitService implements Command {

    private final ComTransDtlLmtRepository comTransDtlLmtRepository;

    private static @NonNull SalaryAdvanceOfferLimitData toSalaryAdvanceOfferLimitData(@Nullable ComTransDtlLmt comTransDtlLmt) {
        var limitData = new SalaryAdvanceOfferLimitData();

        if (comTransDtlLmt != null) {
            limitData.setTransId(comTransDtlLmt.getId());
            BigDecimal approveLimit = getBigDecimalOrDefaultZero(comTransDtlLmt.getRefer_limit_amount());
            BigDecimal usedLimit = getBigDecimalOrDefaultZero(comTransDtlLmt.getUsedLimit());
            limitData.setApproveLimit(comTransDtlLmt.getRefer_limit_amount());
            limitData.setUsedLimit(usedLimit);
            limitData.setRemainingLimit(approveLimit.subtract(usedLimit));
            limitData.setCurrency(comTransDtlLmt.getCurrency());
            if (comTransDtlLmt.getEndDate() != null) {
                limitData.setEndDate(SIMPLE_DATE_FORMAT.format(String.valueOf(comTransDtlLmt.getEndDate())));
            }
        }
        return limitData;
    }

    private static @NonNull BigDecimal getBigDecimalOrDefaultZero(@Nullable BigDecimal comTransDtlLmt) {
        return comTransDtlLmt != null ? comTransDtlLmt : BigDecimal.ZERO;
    }

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CustInfo custInfo = processContext.getCustomer();
        CommonServiceResponse res = new CommonServiceResponse();
        try {
            // hostCif lấy từ custInfo
            String hostCif = custInfo.getHostCifId();

            // call repo COM_LOAN_DISBUR_LMT
            ComTransDtlLmt comTransDtlLmt = comTransDtlLmtRepository.findTopByHostCifIdAndStatusOrderByCreatedAtDesc(hostCif, Constant.STATUS_SUCCESS);

            // Build response
            SalaryAdvanceOfferLimitData limitData = toSalaryAdvanceOfferLimitData(comTransDtlLmt);

            res.setSalaryAdvanceOfferLimitData(limitData);
            processContext.setResponse(res);
        } catch (Exception e) {
            log.error("[DoGetSalaryAdvanceOfferLimitService] exception - hostCif: {}", custInfo.getHostCifId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }
        processContext.setResult(result);
        return !result.isOk();
    }
}
