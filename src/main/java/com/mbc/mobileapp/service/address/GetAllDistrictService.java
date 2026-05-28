package com.mbc.mobileapp.service.address;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.address.DoGetAllDistrict;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GetAllDistrictService extends ChainBase {

    @Autowired
    DoCheckRefNo doCheckRefNo;
    
    @Autowired
    DoGetAllDistrict doGetAllDistrict;
    
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(doGetAllDistrict);
    }
}
