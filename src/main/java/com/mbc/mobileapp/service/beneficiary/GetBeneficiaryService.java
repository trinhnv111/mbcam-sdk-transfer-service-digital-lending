/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 2:47:24 PM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-10-13 2:47:24 PM 
 *  tagId    : mbcam-mobileapp-transfer
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.service.beneficiary;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.beneficiary.DoGetBeneficiary;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;


@Service
public class GetBeneficiaryService extends ChainBase {

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private DoGetBeneficiary doGetBeneficiary;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doGetBeneficiary);
    }
}
