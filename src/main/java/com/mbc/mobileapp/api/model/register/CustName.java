package com.mbc.mobileapp.api.model.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustName {
    
    @JsonProperty("NAME.1")
    private String name1;
}
