package com.mbc.mobileapp.api.model.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustEmail {
    
    @JsonProperty("EMAIL.1")
    private String email;
}
