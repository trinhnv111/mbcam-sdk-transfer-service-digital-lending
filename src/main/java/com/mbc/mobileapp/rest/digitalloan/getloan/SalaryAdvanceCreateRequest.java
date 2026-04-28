package com.mbc.mobileapp.rest.digitalloan.getloan;

import com.mbc.common.rest.bean.BaseRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SalaryAdvanceCreateRequest extends BaseRequest {
    private String tempRecordId; // ID bản ghi tạm (id trong COM_LOAN_DISBUR_LMT)
    private String email;
    private String employmentStartDate; // YYYYMMDD
    private String maritalStatus;
    private String placeOfBirth;
    
    // Address fields (Tỉnh/Huyện/Xã)
    private String currentAddressProvince;
    private String currentAddressDistrict;
    private String currentAddressWard;
}
