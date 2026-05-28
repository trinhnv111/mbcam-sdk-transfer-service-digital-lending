package com.mbc.mobileapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@PropertySources({@PropertySource("classpath:application.properties")})
public class SavingRealTimeDepositConfig {

    @Autowired
    private Environment env;

    public static String CATEGORY;
    public static String PRODUCT_CODE;
    public static String MODULE;
    public static String TAX;
    public static String INTEREST_KEY;
    public static String MONTH;
    public static String YEAR;
    public static String PERCENT;
    public static String MSG_TYPE;
    public static String SAVING_TYPE;
    public static String BRANCH_CODE;

    @PostConstruct
    public void init() {
        CATEGORY = env.getProperty("real.time.deposit.cnfg.category");
        PRODUCT_CODE = env.getProperty("real.time.deposit.cnfg.product.code");
        MODULE = env.getProperty("real.time.deposit.cnfg.module");
        TAX = env.getProperty("real.time.deposit.cnfg.tax");
        INTEREST_KEY = env.getProperty("real.time.deposit.cnfg.interest.key");
        MONTH = env.getProperty("real.time.deposit.cnfg.month");
        YEAR = env.getProperty("real.time.deposit.cnfg.year");
        PERCENT = env.getProperty("real.time.deposit.cnfg.percent");
        MSG_TYPE = env.getProperty("saving.msg.type");
        SAVING_TYPE = env.getProperty("real.time.deposit.cnfg.type");
        BRANCH_CODE = env.getProperty("real.time.deposit.branch.code");

    }

    public static String getInterestRollOverKey(String maturityInstructions) {
        return "2,3".indexOf(maturityInstructions) != -1 ? SavingRealTimeDepositConfig.INTEREST_KEY : null;
    }

}
