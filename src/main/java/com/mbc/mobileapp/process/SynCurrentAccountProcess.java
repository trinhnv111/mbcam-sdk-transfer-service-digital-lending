package com.mbc.mobileapp.process;

import com.mbc.common.entity.Acct;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.AcctRepo;
import com.mbc.common.services.il.nonsavingacct.AccountBase;
import com.mbc.common.services.il.nonsavingacct.ProductInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class SynCurrentAccountProcess extends Thread {
    
    private CustInfo customer;
    
    private List<AccountBase> lstAcctT24;
    
    @Autowired
    private AcctRepo acctRepo;
    
    public void setData(CustInfo customer, List<AccountBase> lstAcctT24) {               
        this.lstAcctT24 = lstAcctT24;
        this.customer = customer;       
    }
    
    @Override
    public void run() {
        AppLog.info(this.getName());
        
        try {
            List<Acct> tblAcct = acctRepo.findByHostCustId(customer.getHostCifId());
            for (int i = 0; i < tblAcct.size(); i++) {
                Acct acct = tblAcct.get(i);
                acct.setInactiveSts(Constant.STATUS_1);
                tblAcct.set(i, acct);
                
            }
            acctRepo.saveAllAndFlush(tblAcct);
            
            List<Acct> synAcct = new ArrayList<Acct>();
            for(AccountBase accountBase : lstAcctT24) {
                ProductInfo productInfo = accountBase.getProductInfoMap().get("Category");
                
             // gan co IsNotify cho account
                Acct acctObj = acctRepo.findByAcctNo(accountBase.getAcctId());
                // dong bo du lieu tu core ve DB app
                if (acctObj != null) {
                    accountBase.setIsNotify(acctObj.getIsNotify());
                    accountBase.setIsDefault(acctObj.getIsDefault());
                    
                    // update du lieu ACCT table tu core
                    acctObj.setAcctNo(accountBase.getAcctId());
                    acctObj.setAcctAlias(accountBase.getAcctnShortName());
                    acctObj.setAcctNm(accountBase.getCustName());
                    acctObj.setAcctTypCd("CA");
                    // acctObj.setAtmNo(accountBase);
                    acctObj.setCategory(productInfo.getId());
                    acctObj.setCcyCd(accountBase.getAcctnCurrency());
                    // acctObj.setCorpId(corpId);
                    acctObj.setCreatedBy(customer.getUserId());
                    acctObj.setCustId(customer.getId());
                    // acctObj.setDebitLmt(debitLmt);
                    acctObj.setHostCustId(accountBase.getCustId());
                    // acctObj.setInactiveBy(inactiveBy);
                    acctObj.setIsAccsEbanking(Constant.YES);
                    acctObj.setIsCrdt(Constant.YES);
                    acctObj.setIsDebit(Constant.YES);
//                    acctObj.setIsInq(Constant.YES);
                    // acctObj.setIsJointHolder(isJointHolder);
                    acctObj.setIsNotify(Constant.YES);
                    acctObj.setInactiveSts(Constant.STATUS_0);
                    // acctObj.setIsParentCrdt(isParentCrdt);
                    // acctObj.setIsParentDebit(isParentDebit);
                    // acctObj.setIsParentInq(isParentInq);
                    acctObj.setOrgUnitCd(accountBase.getBranchInfo().getCode());
                    // acctObj.setPaidAcctNo(paidAcctNo);
                    // acctObj.setPrdTypCd(prdTypCd);
                    // acctObj.setProductType(productType);
                    acctObj.setVersion(new BigDecimal(Constant.CLIENT_RELEASE_VERSION));
                    synAcct.add(acctObj);
                }
                else {

                    // insert du lieu ACCT table tu core
                    acctObj = new Acct();
                    acctObj.setAcctNo(accountBase.getAcctId());
                    acctObj.setAcctAlias(accountBase.getAcctnShortName());
                    acctObj.setAcctNm(accountBase.getCustName());
                    acctObj.setAcctTypCd(accountBase.getBranchInfo().getMnemonic());
                    // acctObj.setAtmNo(accountBase);
                    acctObj.setCategory(productInfo.getId());
                    acctObj.setCcyCd(accountBase.getAcctnCurrency());
                    // acctObj.setCorpId(corpId);
                    acctObj.setCreatedBy(customer.getUserId());
                    acctObj.setCustId(customer.getId());
                    // acctObj.setDebitLmt(debitLmt);
                    acctObj.setHostCustId(accountBase.getCustId());
                    // acctObj.setInactiveBy(inactiveBy);
                    acctObj.setIsAccsEbanking(Constant.YES);
                    acctObj.setIsCrdt(Constant.YES);
                    acctObj.setIsDebit(Constant.YES);
                    acctObj.setIsInq(Constant.YES);
                    // acctObj.setIsJointHolder(isJointHolder);
                    acctObj.setIsNotify(Constant.YES);
                    acctObj.setInactiveSts(Constant.STATUS_0);
                    // acctObj.setIsParentCrdt(isParentCrdt);
                    // acctObj.setIsParentDebit(isParentDebit);
                    // acctObj.setIsParentInq(isParentInq);
                    acctObj.setOrgUnitCd(accountBase.getBranchInfo().getCode());
                    // acctObj.setPaidAcctNo(paidAcctNo);
                    // acctObj.setPrdTypCd(prdTypCd);
                    // acctObj.setProductType(productType);
                    acctObj.setVersion(new BigDecimal(Constant.CLIENT_RELEASE_VERSION));
                    accountBase.setIsNotify(Constant.YES);
                    accountBase.setIsDefault(Constant.NO);
                    synAcct.add(acctObj);
                }
            }
            acctRepo.saveAllAndFlush(synAcct);
            
        }
        catch (Exception e) {
            AppLog.error("ERROR", e);
        }
    }


}
