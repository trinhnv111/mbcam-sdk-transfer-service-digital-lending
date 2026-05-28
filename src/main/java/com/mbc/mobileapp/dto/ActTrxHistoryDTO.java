
package com.mbc.mobileapp.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ActTrxHistoryDTO implements Serializable, Comparable<ActTrxHistoryDTO> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String id;

    private BigDecimal version;

    private Date postingDt;

    private Date effDt;

    private String trxTypCd;

    private String trxCd;

    private String dscp;

    private String additionalDscp;

    private String refNo;

    private String seqNo;

    private BigDecimal debitAmt;

    private BigDecimal creditAmt;

    private BigDecimal availBal;

    private BigDecimal ledgerBal;

    private BigDecimal holdBal;

    private BigDecimal floatBal;

    private String oriTerminalId;

    private String oriUsrId;

    private Date oriTrxDt;

    private String oriOrgUnitCd;

    private String oriChannelCd;

    private String acctNo;

    private String binNo;

    private String vaNo;

    private String vaNm;

    private String chqNo;

    private String debitRefNo;

    private String creditRefNo;

    private BigDecimal principalAmt;

    private String ccyCd;

    private Date trxDt;

    private String beneficiary;

    private String beneficiaryAcct;

    private String stmtId;

    private String trxTyp;

    private String benAcctNm;

    private String bankName;

    private String benAcctNo;

    private String srvcCd;

    private String fromDate;

    private String toDate;

    private String referenceNumber;

    private String amount;

    private String postingDate;

    private String transactionDate;

    private String paymentDetail;

    private String transactionCode;

    private String transactionType;

    private String availableBalance;

    private String accountNumber;

    private String reciprocalAccount;

    private String reciprocalName;

    private String currency;

    private String transactionStatus;

    private String dateTime;

    @Override
    public int compareTo(ActTrxHistoryDTO o) {
        if (getTransactionDate() == null || o.getTransactionDate() == null)
            return 0;
        return getTransactionDate().compareTo(o.getTransactionDate());
    }

    public ActTrxHistoryDTO(String trxTypCd, String trxCd, String dscp, String additionalDscp, String refNo,
        BigDecimal debitAmt, BigDecimal creditAmt, BigDecimal availBal, String acctNo, String ccyCd, Date trxDt,
        String beneficiary, String beneficiaryAcct, String stmtId, String benAcctNm, String bankName, String benAcctNo,
        String srvcCd, String postingDate) {
        super();
        this.trxTypCd = trxTypCd;
        this.trxCd = trxCd;
        this.dscp = dscp;
        this.additionalDscp = additionalDscp;
        this.refNo = refNo;
        this.debitAmt = debitAmt;
        this.creditAmt = creditAmt;
        this.availBal = availBal;
        this.acctNo = acctNo;
        this.ccyCd = ccyCd;
        this.trxDt = trxDt;
        this.beneficiary = beneficiary;
        this.beneficiaryAcct = beneficiaryAcct;
        this.stmtId = stmtId;
        this.benAcctNm = benAcctNm;
        this.bankName = bankName;
        this.benAcctNo = benAcctNo;
        this.srvcCd = srvcCd;
        this.postingDate = postingDate;
    }

    public ActTrxHistoryDTO() {
        super();
    }

}
