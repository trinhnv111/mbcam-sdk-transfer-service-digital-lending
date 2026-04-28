package com.mbc.mobileapp.command.digital_loan.salary_advance;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.validator.base.Validator;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

@Service
public class DoCalculateLimitSalaryAdvance implements Command {
    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;

        // MOCK data limit 1200 USD theo yêu cầu
        Double mockLimit = 1200.00;
        String mockCurrency = "USD";

        context.put("sa_limit", mockLimit);
        context.put("sa_currency", mockCurrency);

        context.setResult(result);
        return !result.isOk();
    }
}
