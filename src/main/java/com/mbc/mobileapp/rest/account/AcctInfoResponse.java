
package com.mbc.mobileapp.rest.account;

import com.mbc.common.dto.AccountInfoByPhone;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AcctInfoResponse extends BaseResponse {

    private List<AccountInfoByPhone> acctDTOList;
}
