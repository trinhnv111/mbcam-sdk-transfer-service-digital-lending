package com.mbc.mobileapp.bean;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AccountTmpConfig {

    public static String BEAU_CHG_ACCT_TMP;

    
    @Autowired
    private Environment env;
    
    @PostConstruct
    public void init() {       

        

    }
}
