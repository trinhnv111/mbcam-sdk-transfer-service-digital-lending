/* ---------------------------------------------------------------------------
 *  All Rights Reserved. Copyright(C) MB Bank, Ltd.
 *  revision : 3:45:04 PM 
 *  vendor   : MB Bank, Ltd.
 *  author   : Le Van Dan OS
 *  since    : 2021-11-22 3:45:04 PM 
 *  tagId    : mbcam-mobileapp-transfer
 * ---------------------------------------------------------------------------
 *  revision marking
 * --------------------------------------------------------------------------- */

package com.mbc.mobileapp.api.model.account.transaction.history;

import lombok.Getter;
import lombok.Setter;

/**
 * @author danlv.os
 *
 */
@Getter
@Setter
public class TransactionHistoryInput {

    private String destination;

    private String version;

    private Message message;
}
