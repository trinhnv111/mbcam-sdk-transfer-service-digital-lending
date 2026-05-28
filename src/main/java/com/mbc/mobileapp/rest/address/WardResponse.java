package com.mbc.mobileapp.rest.address;

import com.mbc.common.dto.Ward;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WardResponse extends BaseResponse {

    public List<Ward> lstWard;
    
}
