package com.mbc.mobileapp.rest.account.history;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mbc.common.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TransHistoryInfo {
    private String accountType;
    private String acctNo;
    private String additionalDscp;
    private double availBal;
    private String bcBankSortCode;
    private String beneficiary;
    private String beneficiaryAcct;
    private String beneficiaryBankCode;
    private String beneficiaryBankName;
    private String binNo;
    private String cateGory;
    private String ccyCd;
    private String chqNo;
    private String cif;
    private Double creditAmt;
    private Double creditRefNo;
    private Double debitAmt;
    private String debitRefNo;
    private String dscp;
    private Double flaotBal;
    private Double holdBal;
    private String kbCitadCode;
    private Double ledgerBal;
    private String oriChannelCd;
    private String oriOrgUnitCd;
    private String oriTerminalId;
    private String oriTrxDt;
    private String oriUsrId;
    private String pos;
    private String postingDt;
    private Double principalAmt;
    private String rciCode;
    private String receivingAddr;
    private String refNo;
    private String seqNo;
    private String stmtId;
    private String timestamp;
    private String transactionType;
    private String trxCd;
    @JsonFormat(pattern = DateUtil.DATETIME_WITH_SLASH, shape = JsonFormat.Shape.STRING, timezone = DateUtil.TIMEZONE_ASIA_HO_CHI_MINH)
    private Date trxDt;
    private String trxTyp;
    private String trxTypCd	;
    private String vaNm;
    private String vaNo;
    private Integer version;
    private String debitAcct;
    private String creditAcct;
    private String debitAcctName;
    private String creditAcctName;
    
    private String sender;
    private String senderAcct;
    private String senderBankCode;
    private String senderBankName;
    
}
