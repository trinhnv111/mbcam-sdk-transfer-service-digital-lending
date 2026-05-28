
package com.mbc.mobileapp.service.user;

import com.mbc.common.command.CheckCustomerState;
import com.mbc.common.command.DoCheckRefNo;
import com.mbc.mobileapp.command.user.login.*;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
//import com.mbc.mobileapp.command.GetFingerPrintByDeviceAndUser;
//import com.mbc.mobileapp.command.UpdateLoginHistory;

@Service
public class LoginService extends ChainBase {

    @Autowired
    private CheckCustomerPassword checkCustomerPassword;

    @Autowired
    private CheckForceChangePassword checkForceChangePassword;

//    @Autowired
//    private GetFingerPrintByDeviceAndUser getFingerPrintByDeviceAndUser;

    @Autowired
    private GetCustomerInformation getCustomerInformation;

//    @Autowired
//    private DoLoginCheckVisaInfo checkVisaInfo;

    @Autowired
    private DoCheckRefNo doCheckRefNo;

    @Autowired
    private CheckCustomerState checkCustomerState;

    @Autowired
    private CheckActiveDevice checkActiveDevice;

//    @Autowired
//    private SaveDeviceToken saveDeviceToken;
    
//    @Autowired
//    private UpdateLoginHistory updateLoginHistory;
    
//    @Autowired
//    private DoPlusLoyaltyPoints doPlusLoyaltyPoints;

    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerPassword);
        addCommand(getCustomerInformation);
//        addCommand(checkVisaInfo);
        addCommand(checkCustomerState);
        addCommand(checkActiveDevice);
//        addCommand(checkForceChangePassword);
//        addCommand(saveDeviceToken);
//        addCommand(updateLoginHistory);
//        addCommand(doPlusLoyaltyPoints);

    }
}
