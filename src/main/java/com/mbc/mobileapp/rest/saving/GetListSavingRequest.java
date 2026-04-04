package com.mbc.mobileapp.rest.saving;

import com.mbc.mobileapp.rest.bean.RestRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetListSavingRequest extends RestRequest {
    
    @Schema(hidden = true)
    private String srvcCd;

}
