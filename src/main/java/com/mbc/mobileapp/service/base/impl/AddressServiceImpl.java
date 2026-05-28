package com.mbc.mobileapp.service.base.impl;

import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.AppLog;
import com.mbc.common.validator.base.Validator;
import com.mbc.mobileapp.rest.address.CountryResponse;
import com.mbc.mobileapp.rest.address.DistrictResponse;
import com.mbc.mobileapp.rest.address.ProvinceResponse;
import com.mbc.mobileapp.rest.address.WardResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import com.mbc.mobileapp.service.address.GetAllCountryService;
import com.mbc.mobileapp.service.address.GetAllDistrictService;
import com.mbc.mobileapp.service.address.GetAllProvinceService;
import com.mbc.mobileapp.service.address.GetAllWardService;
import com.mbc.mobileapp.service.base.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl extends ServiceBase implements AddressService {

    @Autowired
    private GetAllProvinceService getAllProvinceService;

    @Autowired
    private GetAllDistrictService getAllDistrictService;

    @Autowired
    private GetAllWardService getAllWardService;

    @Autowired
    private GetAllCountryService getAllCountryService;

    public CountryResponse getCountry(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        CountryResponse resp = new CountryResponse();
        try {
            getAllCountryService.execute(context);
            logService.execute(context);
            Validator.Result result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
                resp.setLstCountry(response.getLstCountry());
            }
        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        resp.setResult(context.getResult());
        return resp;
    }

    public ProvinceResponse getProvince(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        ProvinceResponse resp = new ProvinceResponse();
        try {
            getAllProvinceService.execute(context);
            logService.execute(context);
            Validator.Result result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
                resp.setLstProvince(response.getLstProvince());
            }
        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        resp.setResult(context.getResult());
        return resp;
    }

    public DistrictResponse getDistrict(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        DistrictResponse resp = new DistrictResponse();
        try {
            getAllDistrictService.execute(context);
            logService.execute(context);
            Validator.Result result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
                resp.setLstDistrict(response.getLstDistrict());
            }
        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        resp.setResult(context.getResult());
        return resp;
    }

    public WardResponse getWard(Request request, CustInfo cust) {
        ProcessContext context = loadContext(request, cust);
        WardResponse resp = new WardResponse();
        try {
            getAllWardService.execute(context);
            logService.execute(context);
            Validator.Result result = context.getResult();
            if (result.isOk()) {
                CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
                resp.setLstWard(response.getLstWard());
            }
        }
        catch (Exception e) {
            AppLog.error(e);
            context.setResult(Validator.Result.UNKNOWN);
        }
        resp.setResult(context.getResult());
        return resp;
    }
}
