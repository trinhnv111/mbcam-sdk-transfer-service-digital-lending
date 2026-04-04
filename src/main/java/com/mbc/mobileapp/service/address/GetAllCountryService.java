package com.mbc.mobileapp.service.address;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.address.DoGetAllCountry;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetAllCountryService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;
    
    @Autowired
    private DoGetAllCountry doGetAllCountry;
    
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGetAllCountry);
    }
}
