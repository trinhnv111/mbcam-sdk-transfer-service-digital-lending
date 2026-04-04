
package com.mbc.mobileapp.rest.beneficiary;

import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.mobileapp.rest.bean.RestRequest;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeleteBeneficiaryRequest extends RestRequest {

    private String benAcctNo;

    private String benAcctName;

    private String benBankCode;

    private String benBankName;

    private String partner;

    private String transType;
}
