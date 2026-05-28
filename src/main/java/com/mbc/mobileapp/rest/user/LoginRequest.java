
package com.mbc.mobileapp.rest.user;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class LoginRequest extends RestRequest {

    @NotNull
    @NotBlank
    private String userId;

    @NotNull
    @NotBlank
    private String pinCode;

    @NotBlank
    @NotNull
    private String deviceId;

//    @NotBlank
//    @NotNull
    private String deviceToken;
    
    @NotNull
    @NotBlank
    private String digitalChannel;
       
    private String phoneId;
    
    private String softTokenId;

    private String fingerPrint;

//    @NotNull
//    @NotBlank
    private String tid;
}
