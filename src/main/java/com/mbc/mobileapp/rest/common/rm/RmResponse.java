package com.mbc.mobileapp.rest.common.rm;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.rm.RmCodeOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RmResponse extends BaseResponse {
    
    private List<RmCodeOutput> info;

}
