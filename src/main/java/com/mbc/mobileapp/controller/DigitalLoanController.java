package com.mbc.mobileapp.controller;


import com.mbc.common.bean.ResponseCode;
import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.DynamicKeyRequest;
import com.mbc.common.rest.bean.DynamicKeyResponse;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.salaryadvance.SaGetLimitInfoRequest;
import com.mbc.mobileapp.rest.salaryadvance.SaGetLimitInfoResponse;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.Validator;


@Slf4j
@RestController
@RequestMapping("/digital-loan")
public class DigitalLoanController extends BaseController {


    private final SalaryAdvanceService salaryAdvanceService;


    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    public DigitalLoanController(Validator validator, SalaryAdvanceService salaryAdvanceService) {
        super(validator);
        this.salaryAdvanceService = salaryAdvanceService;
    }


    @PostMapping("/get-limit-info")
    public SaGetLimitInfoResponse getLimitInfo(
            @RequestBody @Valid DynamicKeyRequest dynRequest,
            HttpServletRequest requestClient) {

        SaGetLimitInfoResponse resp = new SaGetLimitInfoResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
        SaGetLimitInfoRequest param = null;

        // BƯỚC 1: Giải mã DynKey (hoặc decode thẳng nếu disabled)
        if (dynKeyEnabled) {
            DynamicKeyResponse<SaGetLimitInfoRequest> dynResponse =
                    dynDecryptData1(dynRequest, SaGetLimitInfoRequest.class);
            param = dynResponse.getData();
            if (param == null) {
                result = new SimpleResult(
                        dynResponse.getDynResponse().getM_statusCode(), false,
                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
                resp.setResult(result);
                return resp;  // Early return luôn nếu decrypt lỗi
            }
        } else {
            param = mapDataRequestBody(dynRequest.getDataEncrypt(), SaGetLimitInfoRequest.class);
            if (param == null) {
                result = new SimpleResult(
                        ResponseCode.INVALID_INPUT.getDesc(), false,
                        ResponseCode.INVALID_INPUT.getCode());
                resp.setResult(result);
                return resp;
            }
        }

        log.info("[SA GET-LIMIT-INFO] input: {}", JSON.stringify(param));

        // BƯỚC 2: Validate bean (annotation @NotNull, @NotBlank...)
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
            return resp;
        }

        // BƯỚC 3: Lấy CustInfo từ Session (Session giữ token đăng nhập)
        CustInfo cust = getCustFromSession(param.getSessionId());
        if (cust != null) {
            CommonServiceRequest request = new CommonServiceRequest();
            request = (CommonServiceRequest) setBase(request, param);
            request.setPartnerId(requestClient.getUserPrincipal().getName());
            // Gắn request SA vào CommonServiceRequest để chain đọc
            request.setSaGetLimitInfoRequest(param);
            // Khai báo SrvcCd để DoCheckSrvc validate
            request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SA_GET_LIMIT_INFO);

            resp = salaryAdvanceService.getLimitInfo(request, cust);
        }
        resp.setRefNo(param.getRefNo());

        log.info("[SA GET-LIMIT-INFO] output: {}", JSON.stringify(resp));
        return resp;
    }
}
