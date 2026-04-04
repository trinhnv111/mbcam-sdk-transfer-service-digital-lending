package com.mbc.mobileapp.rest.saving.open;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.saving.open.OpenSavingOutput;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenSavingResponse extends BaseResponse {
    private OpenSavingOutput data;
}
