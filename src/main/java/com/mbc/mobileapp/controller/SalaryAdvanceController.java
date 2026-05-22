package com.mbc.mobileapp.controller;

import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.getloan.*;
import com.mbc.mobileapp.service.base.SalaryAdvanceService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/salary-advance")
public class SalaryAdvanceController extends BaseController {
    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    @Autowired
    private SalaryAdvanceService salaryAdvanceService;


    public SalaryAdvanceController(Validator validator) {
        super(validator);
    }


    @ApiOperation("API get digital loans limit")
    @PostMapping("/offer-limit")
    public GetSalaryAdvanceOfferLimitResponse getOfferLimit(@RequestBody BaseRequest param, HttpServletRequest requestClient) {
        GetSalaryAdvanceOfferLimitResponse resp = new GetSalaryAdvanceOfferLimitResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
//        GetSaLimitRequest param;
//        // mã hóa
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<GetSaLimitRequest> dynamicKeyResponse = dynDecryptData1(dynamicKeyRequest, GetSaLimitRequest.class);
//            param = dynamicKeyResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynamicKeyResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynamicKeyRequest.getDataEncrypt(), GetSaLimitRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getCode(), false, ResponseCode.INVALID_INPUT.getDesc());
//                resp.setResult(result);
//            }
//
//        }
//        log.info("[DIGITAL-LOAN GET LIMIT] input data: {}", JSON.stringify(param));
        // validate
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        } else {
            CommonServiceRequest commonServiceRequest = new CommonServiceRequest();
            CustInfo custInfo = getCustFromSession(param.getSessionId());

            if (custInfo != null) {
                // param common
                commonServiceRequest = (CommonServiceRequest) setBase(commonServiceRequest, param);
                Principal principal = requestClient.getUserPrincipal();
                commonServiceRequest.setPartnerId(principal.getName());
                commonServiceRequest.setSrvcCd(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                resp = salaryAdvanceService.getSalaryAdvanceOfferLimit(commonServiceRequest, custInfo);
            }
        }
        resp.setRefNo(param.getRefNo());
        log.info("[SALARY ADVANCE GET OFFER LIMIT] out data: {}", JSON.stringify(resp));
        return resp;
    }


    @ApiOperation("Api init salary advance")
    @PostMapping("/init")
    public SalaryAdvanceInitResponse init(@RequestBody SalaryAdvanceInitRequest param, HttpServletRequest requestClient) {
        SalaryAdvanceInitResponse resp = new SalaryAdvanceInitResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        SalaryAdvanceInitRequest param;
//
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<SalaryAdvanceInitRequest> dynResponse = dynDecryptData1(dynRequest, SalaryAdvanceInitRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), SalaryAdvanceInitRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }
//        log.info("[SDK SALARY ADVANCE INIT] input data: {}", JSON.stringify(param));
        // validation
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        } else {
            CommonServiceRequest request = new CommonServiceRequest();
            CustInfo cust = getCustFromSession(param.getSessionId());
            if (cust != null) {
                // Common param
                request = (CommonServiceRequest) setBase(request, param);
                Principal principal = requestClient.getUserPrincipal();
                request.setPartnerId(principal.getName());
                request.setSalaryAdvanceInitRequest(param);
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                resp = salaryAdvanceService.init(request, cust);
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[SDK SALARY ADVANCE INIT] out data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("Api create salary advance (submit form and gen OTP)")
    @PostMapping("/create")
    public SalaryAdvanceCreateResponse createLoan(@RequestBody SalaryAdvanceCreateRequest param, HttpServletRequest requestClient) {
        SalaryAdvanceCreateResponse resp = new SalaryAdvanceCreateResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        SalaryAdvanceCreateRequest param;
//
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<SalaryAdvanceCreateRequest> dynResponse = dynDecryptData1(dynRequest, SalaryAdvanceCreateRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), SalaryAdvanceCreateRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }
//        log.info("[SDK SALARY ADVANCE CREATE] input data: {}", JSON.stringify(param));
        // validation
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        } else {
            CommonServiceRequest request = new CommonServiceRequest();
            CustInfo cust = getCustFromSession(param.getSessionId());
            if (cust != null) {
                // Common param
                request = (CommonServiceRequest) setBase(request, param);
                Principal principal = requestClient.getUserPrincipal();
                request.setPartnerId(principal.getName());
                request.setSalaryAdvanceCreateRequest(param);
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                resp = salaryAdvanceService.create(request, cust, param.getTokenOTP());
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[SDK SALARY ADVANCE CREATE] out data: {}", JSON.stringify(resp));
        return resp;
    }

}
