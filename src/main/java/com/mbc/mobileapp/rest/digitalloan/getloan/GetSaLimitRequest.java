package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetSaLimitRequest extends BaseRequest {
    private String hostCifId; // lấy từ ss
}
