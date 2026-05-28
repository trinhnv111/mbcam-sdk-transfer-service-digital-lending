package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLmt;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLmtRepository;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.api.model.salary_advance.output.SalaryAdvanceOfferLimitData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@Service
@Slf4j
@RequiredArgsConstructor

public class DoGetSaLimitService implements Command {

    @Autowired
    private final ComTransDtlLmtRepository comTransDtlLmtRepository;

    @Override
    public boolean execute(Context context) throws Exception {
        ProcessContext processContext = (ProcessContext) context;
        Validator.Result result = Validator.Result.OK;
        CustInfo custInfo = processContext.getCustomer();
        CommonServiceRequest request = (CommonServiceRequest) processContext.getRequest();
        try {
            // hostCif lấy từ custInfo
            String hostCif = custInfo.getHostCifId();

            // call repo com_loan_disburment (bảng LIMIT)
            ComTransDtlLmt comTransDtlLmt = comTransDtlLmtRepository.
                    findTopByHostCifIdAndStatusOrderByCreatedAtDesc(hostCif, Constant.STATUS_SUCCESS);

            // Build data trả về
            SalaryAdvanceOfferLimitData limitData = getSaLimitData(comTransDtlLmt);

            CommonServiceResponse res = new CommonServiceResponse();
            res.setSalaryAdvanceOfferLimitData(limitData);
            processContext.setResponse(res);

//            return true;

        } catch (Exception e) {
            log.error("[DoGetSaLimitService] exception - hostCif: {}", custInfo != null ? custInfo.getHostCifId() : "null", e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false, ResponseCode.TRANSACTION_FAIL.getCode());
        }

        processContext.setResult(result);
        return !result.isOk();
    }

    @NotNull
    private static SalaryAdvanceOfferLimitData getSaLimitData(ComTransDtlLmt comTransDtlLmt) {
        SalaryAdvanceOfferLimitData limitData = new SalaryAdvanceOfferLimitData();

        if (comTransDtlLmt != null) {
            BigDecimal approveLimit = comTransDtlLmt.getRefer_limit_amount() != null ? comTransDtlLmt.getRefer_limit_amount() : BigDecimal.ZERO;
            BigDecimal usedLimit = comTransDtlLmt.getUsedLimit() != null ? comTransDtlLmt.getUsedLimit() : BigDecimal.ZERO;
            limitData.setApproveLimit(comTransDtlLmt.getRefer_limit_amount());
            limitData.setUsedLimit(usedLimit);
            limitData.setRemainingLimit(approveLimit.subtract(usedLimit));
            limitData.setCurrency(comTransDtlLmt.getCurrency());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            if (comTransDtlLmt.getStartDate() != null) {
//                limitData.setStartDate(sdf.format(comTransDtlLmt.getStartDate()));
//            }
            if (comTransDtlLmt.getEndDate() != null) {
                limitData.setEndDate(sdf.format(comTransDtlLmt.getEndDate()));
            }
        } else {
            // Chưa đăng ký hạn mức → trả về 0
            limitData.setApproveLimit(BigDecimal.ZERO);
            limitData.setUsedLimit(BigDecimal.ZERO);
            limitData.setRemainingLimit(BigDecimal.ZERO);
            limitData.setCurrency(null);
        }
        return limitData;
    }
}
