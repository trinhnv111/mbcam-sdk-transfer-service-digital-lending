package com.mbc.mobileapp.service.register;

import com.mbc.common.command.DoCheckRefNo;
import com.mbc.common.command.pushnotifypartner.DoPushNotifyPartner;
import com.mbc.mobileapp.command.register.creare.*;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class CreateCustomerService extends ChainBase {

	@Autowired
	private DoCheckRefNo doCheckRefNo;

	@Autowired
	private DoCustomerCheckAML doCustomerCheckAML;
//
	@Autowired
	private DoCreateCustomerValidate doCreateCustomerValidate;

//	@Autowired
//	private ValidateOTP validateOTP;

	@Autowired
	private DoCreateCustomerCif doCreateCustomerCif;

	@Autowired
	private DoCreateNonSavingAccount doCreateNonSavingAccount;

	@Autowired
	private DoOpenEbanking doOpenEbanking;

	@Autowired
	private DoCheckPincode doCheckPincode;

	@Autowired
	private DoRegisterAcctLoyalty doRegisterAcctLoyalty;

//	@Autowired
//    private DoPlusLoyaltyPoints doPlusLoyaltyPoints;

//	@Autowired
//	private DoEarnPointReferral doEarnPointReferral;
    @Autowired
	private DoPushNotifyPartner doPushNotifyPartner;

	@PostConstruct
	public void commandChain() {
		addCommand(doCheckRefNo);
		addCommand(doCheckPincode);
		addCommand(doCustomerCheckAML);
		addCommand(doCreateCustomerValidate);
////		addCommand(validateOTP);
		addCommand(doCreateCustomerCif);
		addCommand(doCreateNonSavingAccount);
		addCommand(doOpenEbanking);
		addCommand(doPushNotifyPartner);
//		addCommand(doRegisterAcctLoyalty);
////		addCommand(doPlusLoyaltyPoints);
//		addCommand(doEarnPointReferral);
	}
}
