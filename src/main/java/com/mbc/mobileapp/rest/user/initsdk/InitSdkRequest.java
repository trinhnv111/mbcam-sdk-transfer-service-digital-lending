
package com.mbc.mobileapp.rest.user.initsdk;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class InitSdkRequest extends RestRequest {

    private InitSdkInfo info;
}
