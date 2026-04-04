
package com.mbc.mobileapp.service.transfer.inhouse;

import com.mbc.common.command.*;
import com.mbc.mobileapp.command.transfer.DoCheckCreditAccount;
import com.mbc.mobileapp.command.transfer.DoValidateTransfer;
import org.apache.commons.chain.impl.ChainBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ValidTransferService extends ChainBase {

  @Autowired
  private DoCheckRefNo doCheckRefNo;
  
  @Autowired
  private DoCheckSrvc doCheckSrvc;
  
  @Autowired
  private DoCheckDebitAccount doCheckDebitAccount;
  
  @Autowired
  private DoCheckCreditAccount doCheckCreditAccount;
  
  @Autowired
  private CheckCustomerState checkCustomerState ;
  
  @Autowired
  private DoValidateTransfer doValidateTransfer;
  
  @Autowired
  private DoGetLimitEasyPaymentUsed doGetLimitEasyPaymentUsed ;

  
    @PostConstruct
    public void addCommandChain() {
        addCommand(doCheckRefNo);
        addCommand(checkCustomerState);
        addCommand(doCheckSrvc);
        addCommand(doCheckDebitAccount);
        addCommand(doCheckCreditAccount);
        addCommand(doValidateTransfer);
        addCommand(doGetLimitEasyPaymentUsed);
    }
}
