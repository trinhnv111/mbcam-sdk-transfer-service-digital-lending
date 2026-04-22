package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;

@Data

public class GetSaLimitRequest extends BaseRequest {
    private String hostCifId; // lấy từ ss
}
