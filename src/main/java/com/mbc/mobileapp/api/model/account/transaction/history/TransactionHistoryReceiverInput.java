package com.mbc.mobileapp.api.model.account.transaction.history;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionHistoryReceiverInput extends TransactionHistoryDetailInput {
    private String refNo;
    private String oriTerminalId;
    private String transactionType;

}
