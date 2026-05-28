package com.mbc.mobileapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@Configuration
@PropertySources({@PropertySource("classpath:application.properties")})
public class SavingFlexiDepositConfig {

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
    public static String SUB_PRODUCT_CODE;
    public static int MATURITY_BACKDATE = 60;

    @PostConstruct
    public void init() {
        CATEGORY = env.getProperty("flexi.deposit.cnfg.category");
        PRODUCT_CODE = env.getProperty("flexi.deposit.cnfg.product.code");
        MODULE = env.getProperty("flexi.deposit.cnfg.module");
        TAX = env.getProperty("flexi.deposit.cnfg.tax");
        INTEREST_KEY = env.getProperty("flexi.deposit.cnfg.interest.key");
        MONTH = env.getProperty("flexi.deposit.cnfg.month");
        YEAR = env.getProperty("flexi.deposit.cnfg.year");
        PERCENT = env.getProperty("flexi.deposit.cnfg.percent");
        MSG_TYPE = env.getProperty("saving.msg.type");
        SAVING_TYPE = env.getProperty("flexi.deposit.cnfg.type");
        BRANCH_CODE = env.getProperty("flexi.deposit.branch.code");
        SUB_PRODUCT_CODE = env.getProperty("flexi.deposit.cnfg.subproduct.code");
        MATURITY_BACKDATE = Integer.parseInt(env.getProperty("flexi.deposit.cnfg.maturitybackdate"));
    }

    public static String getInterestRollOverKey(String maturityInstructions) {
        return "2,3".indexOf(maturityInstructions) != -1 ? SavingFlexiDepositConfig.INTEREST_KEY : null;
    }
}
