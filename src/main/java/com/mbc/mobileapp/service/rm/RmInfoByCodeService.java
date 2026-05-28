package com.mbc.mobileapp.service.rm;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.rm.DoGetRmInfo;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RmInfoByCodeService extends ChainBase {
    
    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private DoGetRmInfo doGetRmInfo;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGetRmInfo);
    }
}
