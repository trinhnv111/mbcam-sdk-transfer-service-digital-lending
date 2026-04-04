package com.mbc.mobileapp.rest.address;

import com.mbc.common.dto.District;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DistrictResponse extends BaseResponse {

    public List<District> lstDistrict;
}
