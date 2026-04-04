package com.mbc.mobileapp.service.common;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.common.DoGenerateToken;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GenerateTokenService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoGenerateToken doGenerateToken;


    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGenerateToken);

    }
}
