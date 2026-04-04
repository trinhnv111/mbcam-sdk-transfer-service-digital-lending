package com.mbc.mobileapp.service.address;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.address.DoGetAllProvince;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetAllProvinceService extends ChainBase {

    @Autowired
    DoCheckRefNo doCheckRefNo;
    
    @Autowired
    DoGetAllProvince doGetAllProvince;
    
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGetAllProvince);
    }
    
}
