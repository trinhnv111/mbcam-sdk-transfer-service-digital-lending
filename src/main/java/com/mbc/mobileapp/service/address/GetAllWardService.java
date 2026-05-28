package com.mbc.mobileapp.service.address;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.address.DoGetAllWard;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetAllWardService extends ChainBase {

    @Autowired
    DoCheckRefNo doCheckRefNo;
    
    @Autowired
    DoGetAllWard doGetAllWard;
    
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGetAllWard);
    }
}
