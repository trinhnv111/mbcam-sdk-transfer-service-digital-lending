package com.mbc.mobileapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@PropertySources({@PropertySource("classpath:application.properties")})
public class SavingFixedDepositConfig {

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
        CATEGORY = env.getProperty("saving.deposit.cnfg.category");
        PRODUCT_CODE = env.getProperty("saving.deposit.cnfg.product.code");
        MODULE = env.getProperty("saving.deposit.cnfg.module");
        TAX = env.getProperty("saving.deposit.cnfg.tax");
        INTEREST_KEY = env.getProperty("saving.deposit.cnfg.interest.key");
        MONTH=env.getProperty("saving.deposit.cnfg.month");
        YEAR=env.getProperty("saving.deposit.cnfg.year");
        PERCENT=env.getProperty("saving.deposit.cnfg.percent");
        MSG_TYPE=env.getProperty("saving.msg.type");
        SAVING_TYPE=env.getProperty("saving.deposit.cnfg.type");
        BRANCH_CODE=env.getProperty("saving.deposit.branch.code");

    }

}
