package com.mbc.mobileapp.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenData {

    private String partner;

    private String auth;

    private String tid;

}
