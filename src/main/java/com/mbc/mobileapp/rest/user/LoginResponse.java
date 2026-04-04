
package com.mbc.mobileapp.rest.user;

import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse extends BaseResponse {

    private String sessionId;

//    private String fingerPrint;
//
//    private String viaPhone;
//
    private CustInfo cust;
    
    private LoginCustomerInfo custInfo;

//    private String bioId;
}
