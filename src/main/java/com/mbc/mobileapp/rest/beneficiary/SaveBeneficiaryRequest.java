
package com.mbc.mobileapp.rest.beneficiary;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Setter
@Getter
public class SaveBeneficiaryRequest extends RestRequest {

    private BeneInfo beneInfo;
}
