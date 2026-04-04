
package com.mbc.mobileapp.api.model.transfer.ciftp;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CiftpMakeConfigInfo {

    private String amountMax;

    private String amountMin;

    private String channel;

    private String currency;

}
