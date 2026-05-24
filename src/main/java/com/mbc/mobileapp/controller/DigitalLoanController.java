package com.mbc.mobileapp.controller;

import com.mbc.common.bean.ResponseCode;
import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.DynamicKeyRequest;
import com.mbc.common.rest.bean.DynamicKeyResponse;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.common.validator.base.Validator.Result;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.digitalloan.disbursement.*;
import com.mbc.mobileapp.rest.digitalloan.getloan.*;

import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentRequest;
import com.mbc.mobileapp.rest.digitalloan.repayment.LoanRepaymentResponse;
import com.mbc.mobileapp.service.base.DigitalLoanService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.Validator;
import java.security.Principal;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/digital-loan")
public class DigitalLoanController extends BaseController {

    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    @Autowired
    private DigitalLoanService digitalLoanService;

    public DigitalLoanController(Validator validator) {
        super(validator);
    }

    /**
     * Danh sách khoản vay (loanListInfor — ms-loan {@code GET /loan/v2.0/get-loan}).
     *
     * @param param         body: session + refNo + optional {@code ldId}
     * @param requestClient HttpServletRequest
     * @return GetLoanResponse (data = {@link com.mbc.mobileapp.api.model.digitalloan.v2.MsLoanData})
     */
    @ApiOperation("Api get loan")
    @PostMapping("/get-ld")
    public GetLoanResponse getLoan(@RequestBody GetLoanRequest param, HttpServletRequest requestClient) {
        GetLoanResponse resp = new GetLoanResponse();
        Result result;
//        GetLoanRequest param;
//
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<GetLoanRequest> dynResponse = dynDecryptData1(dynRequest, GetLoanRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), GetLoanRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }
//        log.info("[SDK DIGITAL-LOAN GET Loan] input data: {}", JSON.stringify(param));
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
                request.setGetLoanRequest(param);
//                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_DIGITAL_LOAN);
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                resp = digitalLoanService.getLoan(request, cust);
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[DIGITAL-LOAN GET Loan] out data: {}", JSON.stringify(resp));
        return resp;
    }

    /**
     * Danh sách khoản vay quá hạn
     *
     * @param dynRequest    DynamicKeyRequest
     * @param requestClient HttpServletRequest
     * @return GetPdResponse
     */
    @ApiOperation("Api get past due / khoan vay qua han")
    @PostMapping("/get-pd")
    public GetPdResponse getPd(@RequestBody @Valid DynamicKeyRequest dynRequest, HttpServletRequest requestClient) {
        GetPdResponse resp = new GetPdResponse();
        Result result;
        GetPdRequest param;
        if (dynKeyEnabled) {
            DynamicKeyResponse<GetPdRequest> dynResponse = dynDecryptData1(dynRequest, GetPdRequest.class);
            param = dynResponse.getData();
            if (param == null) {
                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
                resp.setResult(result);
            }
        } else {
            param = mapDataRequestBody(dynRequest.getDataEncrypt(), GetPdRequest.class);
            if (param == null) {
                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
                resp.setResult(result);
            }
        }
        log.info("[DIGITAL-LOAN GET PD] input data: {}", JSON.stringify(param));
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
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_DIGITAL_LOAN);
                resp = digitalLoanService.getPd(request, cust);
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[DIGITAL-LOAN GET PD] out data: {}", JSON.stringify(resp));
        return resp;
    }

    /**
     * Lịch sử trả nợ khoản vay
     *
     * @param requestClient HttpServletRequest
     * @return GetPaymentHistoryResponse
     */
    @ApiOperation("Api get payment history")
    @PostMapping("/get-payment-history")
    public GetPaymentHistoryResponse getPaymentHistory(@RequestBody GetPaymentHistoryRequest param, HttpServletRequest requestClient) {
        GetPaymentHistoryResponse resp = new GetPaymentHistoryResponse();
        Result result;
//        GetPaymentHistoryRequest param;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<GetPaymentHistoryRequest> dynResponse = dynDecryptData1(dynRequest, GetPaymentHistoryRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), GetPaymentHistoryRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }
//        log.info("[DIGITAL-LOAN GET PAYMENT-HISTORY] input data: {}", JSON.stringify(param));
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
                request.setPaymentRequest(param.getData());
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                resp = digitalLoanService.getPaymentHistory(request, cust);

            }
        }
        resp.setRefNo(param.getRefNo());
        log.info("[DIGITAL-LOAN GET PAYMENT-HISTORY] out data: {}", JSON.stringify(resp));
        return resp;
    }

    /**
     * Trả dư nợ khoản vay
     *
     * @param dynRequest    DynamicKeyRequest
     * @param requestClient HttpServletRequest
     * @return LoanRepaymentResponse
     */
    @PostMapping("/loan-repayment")
    public LoanRepaymentResponse loanRepayment(@Valid @RequestBody DynamicKeyRequest dynRequest, HttpServletRequest requestClient) {
        LoanRepaymentResponse resp = new LoanRepaymentResponse();
        Result result;
        LoanRepaymentRequest param;
        if (dynKeyEnabled) {
            DynamicKeyResponse<LoanRepaymentRequest> dynResponse = dynDecryptData1(dynRequest, LoanRepaymentRequest.class);
            param = dynResponse.getData();
            if (param == null) {
                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false, ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
                resp.setResult(result);
            }
        } else {
            param = mapDataRequestBody(dynRequest.getDataEncrypt(), LoanRepaymentRequest.class);
            if (param == null) {
                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
                resp.setResult(result);
            }
        }
        // validation
        log.info("[LOAN REPAYMENT] input data: {}", JSON.stringify(resp));
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        } else {
            CommonServiceRequest request = new CommonServiceRequest();
            CustInfo cust = getCustFromSession(param.getSessionId());
            if (cust != null) {
                // Common param
                request.setLoanRepaymentRequest(param);
                request.setSourceAccountNumber(param.getDebitAccount());
                request.setDestAccountCurrency(param.getDebitAccountCurrency());
                request = (CommonServiceRequest) setBase(request, param);
                Principal principal = requestClient.getUserPrincipal();
                request.setPartnerId(principal.getName());
                resp = digitalLoanService.repayment(request, cust, param.getTokenOTP());
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[LOAN REPAYMENT] output data: {}", JSON.stringify(resp));
        return resp;
    }

    /**
     * Kiểm tra lệnh giải ngân
     *
     * @param param         ValidDisbursementRequest
     * @param requestClient HttpServletRequest
     * @return ValidDisbursementReponse
     */
    @ApiOperation("Api valid disbursement")
    @PostMapping("/valid-disbursement")
    public ValidDisbursementResponse loanValidDisbursement(@RequestBody @Valid ValidDisbursementRequest param, HttpServletRequest requestClient) {
        ValidDisbursementResponse resp = new ValidDisbursementResponse();
        Result result;
        // if (dynKeyEnabled) {
        // DynamicKeyResponse<GetLoanInfoRequest> dynResponse = dynDecryptData1(dynRequest, GetLoanInfoRequest.class);
        // dynRequest = dynResponse.getData();
        //
        // if (param == null) {
        // result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
        // ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
        // resp.setResult(result);
        //
        // }
        // } else {
        // param = mapDataRequestBody(dynRequest.getDataEncrypt(), TransferToWalletRequest.class);
        // if (param == null) {
        // result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
        // ResponseCode.INVALID_INPUT.getCode());
        // resp.setResult(result);
        // }
        // }

        log.info("[LOAN VALID DISBURSEMENT] input data: {}", JSON.stringify(param));
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
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                request.setValidDisbursementRequest(param);
                resp = digitalLoanService.validDisbursement(request, cust);
            }
        }
        resp.setRefNo(param.getRefNo());
        log.info("[LOAN VALID DISBURSEMENT] out data: {}", JSON.stringify(resp));
        return resp;
    }

    /**
     * Giải ngân khoản vay
     *
     * @param param         DisbursementRequest
     * @param requestClient HttpServletRequest
     * @return DisbursementResponse
     */
    @ApiOperation("Api disbursement")
    @PostMapping("/disbursement")
    public DisbursementResponse<Object> loanDisbursement(@RequestBody @Valid DisbursementRequest param, HttpServletRequest requestClient) {
        DisbursementResponse<Object> resp = new DisbursementResponse<Object>();
        Result result = null;
        // if (dynKeyEnabled) {
        // DynamicKeyResponse<GetLoanInfoRequest> dynResponse = dynDecryptData1(dynRequest, GetLoanInfoRequest.class);
        // dynRequest = dynResponse.getData();
        //
        // if (param == null) {
        // result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
        // ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
        // resp.setResult(result);
        //
        // }
        // } else {
        // param = mapDataRequestBody(dynRequest.getDataEncrypt(), TransferToWalletRequest.class);
        // if (param == null) {
        // result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
        // ResponseCode.INVALID_INPUT.getCode());
        // resp.setResult(result);
        // }
        // }
        log.info("[LOAN DISBURSEMENT] input data: {}", JSON.stringify(param));
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
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_LOAN_OD_DISBURSEMENT);
                request.setTransId(param.getTransId());
                // Gắn toàn bộ disbursementRequest để chain đọc
                request.setDisbursementRequest(param);
                // Token OTP cho ValidateOTP command
                if (param.getTokenOTP() != null) {
                    request.setTokenOTP(param.getTokenOTP());
                }
                resp = digitalLoanService.disbursement(request, cust);

            }
        }
        resp.setRefNo(param.getRefNo());
        log.info("[LOAN DISBURSEMENT] out data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("Api Get Fee - Get Information ")
    @PostMapping("/get-confirm")
    public DisbursementInformationResponse disbursementInformation(@RequestBody @Valid DisbursementInformationRequest param, HttpServletRequest requestClient) {
        DisbursementInformationResponse resp = new DisbursementInformationResponse();
        Result result = null;
        // if (dynKeyEnabled) {
        // DynamicKeyResponse<GetLoanInfoRequest> dynResponse = dynDecryptData1(dynRequest, GetLoanInfoRequest.class);
        // dynRequest = dynResponse.getData();
        //
        // if (param == null) {
        // result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
        // ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
        // resp.setResult(result);
        //
        // }
        // } else {
        // param = mapDataRequestBody(dynRequest.getDataEncrypt(), TransferToWalletRequest.class);
        // if (param == null) {
        // result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
        // ResponseCode.INVALID_INPUT.getCode());
        // resp.setResult(result);
        // }
        // }
        log.info("[LOAN DISBURSEMENT] input data: {}", JSON.stringify(param));
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
                request.setDigitalLoanRequest(param);
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                request.setTransId(param.getTransId());
                resp = digitalLoanService.disbursementInformation(request, cust);

            }
        }
        resp.setRefNo(param.getRefNo());
        log.info("[LOAN DISBURSEMENT] out data: {}", JSON.stringify(resp));
        return resp;
    }

    /**
     * Tạo hợp đồng giải ngân (PDF ENG + KHR) và upload lên FTS
     * Flow: SDK BE → BIRT → FTS → (ECM) → docId
     *
     * @param requestClient HttpServletRequest
     * @return DisbursementResponse (data = DoGenFileOutput chứa fileContentEng + fileContentKhr)
     */
    @ApiOperation("Api gen file contract disbursement")
    @PostMapping("/genfile")
    public DisbursementResponse<Object> genFile(@RequestBody GenFileRequest param,
                                                HttpServletRequest requestClient) {
        DisbursementResponse<Object> resp = new DisbursementResponse<>();
        Result result;

//        /* TODO: Bật mã hóa DynamicKey khi production
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<GenFileRequest> dynResponse = dynDecryptData1(dynRequest, GenFileRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//                return resp;
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), GenFileRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//                return resp;
//            }
//        }
//        */

        log.info("[LOAN GENFILE] input data: {}", JSON.stringify(param));
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        } else {
            CustInfo cust = getCustFromSession(param.getSessionId());
            if (cust != null) {
                CommonServiceRequest request = new CommonServiceRequest();
                Principal principal = requestClient.getUserPrincipal();

                // Tạo refNo theo pattern: userId-yyyyMMddHHmmssSSS
                LocalDateTime now = LocalDateTime.now();
                String refNo = String.format("%s-%d%02d%02d%02d%02d%02d%d",
                        cust.getUserId(),
                        now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                        now.getHour(), now.getMinute(), now.getSecond(),
                        now.getNano() / 1_000_000);
                param.setRefNo(refNo);

                request = (CommonServiceRequest) setBase(request, param);
                request.setPartnerId(principal.getName());
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SALARY_ADVANCE);
                request.setTransId(param.getTransId());
                request.setGenFileRequest(param);
                resp = digitalLoanService.genFile(request, cust);
                resp.setRefNo(param.getRefNo());
            }
        }
        log.info("[LOAN GENFILE] out data: {}", JSON.stringify(resp));
        return resp;
    }

}
