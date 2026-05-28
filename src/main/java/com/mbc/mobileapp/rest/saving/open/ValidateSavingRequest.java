package com.mbc.mobileapp.rest.saving.open;

import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;

@Getter
@Setter
public class ValidateSavingRequest extends RestRequest {

    @Valid
    private SavingInfo savingInfo;
}
