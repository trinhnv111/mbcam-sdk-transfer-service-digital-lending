/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 2:25:28 PM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-10-13 2:25:28 PM 
 *  tagId    : mbcam-mobileapp-transfer
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * @author danlv.os
 *
 */
@Setter
@Getter
public class ComBeneficiaryDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String amount;

    private String benAcctName;

    private String benAcctNo;

    private String benAddress;

    private String benBankAddress;

    private String benBankCode;

    private String benBankMnemonic;

    private String benBankName;

    private String benBankSwift;

    private String ccyCd;

    private String chargeAcctNo;

    private String createdBy;

    private Date createdDt;

    private String custId;

    private String debitAcctNo;

    private String description;

    private String id;

    private String isDelete;

    private String mediateBankAddress;

    private String mediateBankName;

    private String mediateBankSwift;

    private String purpose;

    private String routingNo;

    private String suggestName;

    private String transtype;

    private String type;

    private String updatedBy;

    private Date updatedDt;

}
