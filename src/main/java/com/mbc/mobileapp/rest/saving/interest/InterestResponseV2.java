package com.mbc.mobileapp.rest.saving.interest;

import com.mbc.common.rest.bean.BaseResponse;
import com.mbc.mobileapp.api.model.saving.interest.InterestOutput;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InterestResponseV2 extends BaseResponse {
    private List<InterestOutput> data = new ArrayList<>();
}
