package com.mbc.mobileapp.rest.saving;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.object.ProductSavingV2;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetListSavingResponse extends BaseResponse {

    private List<ProductSavingV2> lstProductSaving;
}
