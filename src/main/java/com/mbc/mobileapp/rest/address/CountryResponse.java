package com.mbc.mobileapp.rest.address;

import com.mbc.common.dto.Country;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CountryResponse extends BaseResponse {
    public List<Country> lstCountry;
}
