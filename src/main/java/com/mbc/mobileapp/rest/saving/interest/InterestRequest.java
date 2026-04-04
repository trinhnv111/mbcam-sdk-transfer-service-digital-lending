package com.mbc.mobileapp.rest.saving.interest;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InterestRequest extends RestRequest {
    
    private boolean isInterestRate;
}
