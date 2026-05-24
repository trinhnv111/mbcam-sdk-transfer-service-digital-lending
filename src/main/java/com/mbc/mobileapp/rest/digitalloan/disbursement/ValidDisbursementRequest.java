package com.mbc.mobileapp.rest.digitalloan.disbursement;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Request cho POST /digital-loan/valid-disbursement và POST /genfile
 *
 * transId: FE gửi lên để cross-check với hạn mức active của KH.
 * BE query bằng hostCifId (session) + loanType + status=SUCCESS,
 * sau đó verify lmt.getId() == transId để đảm bảo KH đang thao tác đúng khoản vay của mình.
 */
@Data
public class ValidDisbursementRequest extends BaseRequest {
    @NotBlank
    @NotNull
    private String transId;
}
