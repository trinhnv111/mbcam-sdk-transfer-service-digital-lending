package com.mbc.mobileapp.constant;

import com.mbc.common.microservice.base.MicroserviceConstantUrl;
import com.mbc.common.util.Constant;
import com.mbc.mobileapp.config.HostUrlConfig;

public enum ConstantUrl implements MicroserviceConstantUrl {

    EXECUTE_T24_ROUTINE("/t24-query/v1.0/routine", Constant.POST, "execute t24 routine", HostUrlConfig.IL_REST_URL),
    GET_T24_CUST_INFO("/customer/v1.0/", Constant.GET, "get t24 customer information", HostUrlConfig.GET_CUSTOMER_T24_REST_URL),
    EXECUTE_T24_VERSION("/t24-query/v1.0/version", Constant.POST, "execute t24 version", HostUrlConfig.IL_T24_VERSION_REST_URL),
    CARD_GW_WAY4("/integration-way4-cardgw/v1.0", Constant.POST, "execute t24 version", HostUrlConfig.CARD_GW_IL_REST_URL),
//    GET_NON_SAVING_ACCOUNT("/customer/1.0/inquiry", Constant.POST, "Get non saving account", "http://10.1.37.27:10471"),
    ;

    private ConstantUrl(String url, String method, String desc, Boolean isparam) {
        this.url = url;
        this.desc = desc;
        this.method = method;
        this.isparam = isparam;
    }

    private ConstantUrl(String url, String method, String desc) {
        this.url = url;
        this.desc = desc;
        this.method = method;
    }

    private ConstantUrl(String url, String method, String desc, String host) {
        this.url = url;
        this.desc = desc;
        this.method = method;
        this.host = host;
    }

    @Override
    public Boolean getIsparam() {
        return isparam;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String getMethod() {
        return method;
    }

    private String url;

    private String desc;

    private String method;

    private String host;

    private Boolean isparam = false;

    @Override
    public String getHost() {
        return host;
    }
}
