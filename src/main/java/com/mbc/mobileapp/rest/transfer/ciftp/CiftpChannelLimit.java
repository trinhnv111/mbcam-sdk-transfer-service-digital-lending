package com.mbc.mobileapp.rest.transfer.ciftp;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CiftpChannelLimit {
    
    private String ncs;
    private String largeValue;
    private String retail;

}
