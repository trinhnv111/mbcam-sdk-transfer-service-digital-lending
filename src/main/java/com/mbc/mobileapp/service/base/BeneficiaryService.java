package com.mbc.mobileapp.service.base;

import com.mbc.common.bean.Request;
import com.mbc.common.object.CustInfo;
import com.mbc.mobileapp.rest.beneficiary.BeneficiaryResponse;
import com.mbc.mobileapp.rest.beneficiary.SaveBeneficiaryResponse;

public interface BeneficiaryService {

    public BeneficiaryResponse getBeneficiaryList(Request request, CustInfo cust);

    public SaveBeneficiaryResponse saveBeneficiary(Request request, CustInfo cust);
}
