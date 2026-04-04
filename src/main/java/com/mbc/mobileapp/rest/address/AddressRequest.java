
package com.mbc.mobileapp.rest.address;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressRequest extends BaseRequest {

    private AddressInfo info;

}
