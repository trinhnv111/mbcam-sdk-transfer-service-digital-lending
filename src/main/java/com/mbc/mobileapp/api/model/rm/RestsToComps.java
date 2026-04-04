package com.mbc.mobileapp.api.model.rm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestsToComps {

    @JsonAlias("RESTR.TO.COMPS")
    @JsonProperty("restsToComps")
    private String restsToComps;
}
