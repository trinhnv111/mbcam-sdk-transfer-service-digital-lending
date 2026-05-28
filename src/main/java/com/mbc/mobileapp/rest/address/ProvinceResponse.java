package com.mbc.mobileapp.rest.address;

import com.mbc.common.dto.Province;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProvinceResponse extends BaseResponse {

    public List<Province> lstProvince;
}
