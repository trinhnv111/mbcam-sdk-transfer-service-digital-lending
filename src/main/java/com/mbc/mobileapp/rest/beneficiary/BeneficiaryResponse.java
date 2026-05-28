
package com.mbc.mobileapp.rest.beneficiary;

import com.mbc.common.dto.ComBeneficiaryDTO;
import com.mbc.common.rest.bean.BaseResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BeneficiaryResponse extends BaseResponse {

  private List<ComBeneficiaryDTO> beneficiaryDTOList;
}
