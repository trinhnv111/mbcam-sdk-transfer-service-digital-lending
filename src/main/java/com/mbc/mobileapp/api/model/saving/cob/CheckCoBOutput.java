package com.mbc.mobileapp.api.model.saving.cob;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CheckCoBOutput {
    private  String t24VersionId;
    @JsonAlias({"SERVICE.CONTROL"})
    @JsonProperty("SERVICE.CONTROL")
    private String serviceControl;
}
