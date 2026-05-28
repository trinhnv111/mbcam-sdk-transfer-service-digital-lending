package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;

import com.mbc.mobileapp.rest.address.CountryResponse;
import com.mbc.mobileapp.rest.address.DistrictResponse;
import com.mbc.mobileapp.rest.address.ProvinceResponse;
import com.mbc.mobileapp.rest.address.WardResponse;

public interface AddressService {

    public CountryResponse getCountry(Request request, CustInfo cust);

    public ProvinceResponse getProvince(Request request, CustInfo cust);

    public DistrictResponse getDistrict(Request request, CustInfo cust);

    public WardResponse getWard(Request request, CustInfo cust);

}
