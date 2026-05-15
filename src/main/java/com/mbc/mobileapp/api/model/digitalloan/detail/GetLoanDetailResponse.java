package com.mbc.mobileapp.api.model.digitalloan.detail;

import com.mbc.common.rest.bean.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetLoanDetailResponse extends BaseResponse {

    private GetLoanDetailData data;
}
