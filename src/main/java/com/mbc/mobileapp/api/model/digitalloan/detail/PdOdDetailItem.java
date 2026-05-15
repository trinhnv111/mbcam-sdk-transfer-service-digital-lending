package com.mbc.mobileapp.api.model.digitalloan.detail;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdOdDetailItem {
    @JsonAlias({"pdId", "pdID"})
    private String pdId;
}
