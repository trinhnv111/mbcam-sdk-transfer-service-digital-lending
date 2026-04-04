package com.mbc.mobileapp.controller;


import com.mbc.common.bean.ResponseCode;
import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.rest.bean.BaseRequest;
import com.mbc.common.rest.bean.DynamicKeyRequest;
import com.mbc.common.rest.bean.DynamicKeyResponse;
import com.mbc.common.util.AppLog;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.rest.account.AccountSavingRequest;
import com.mbc.mobileapp.rest.account.AccountSavingResponse;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.saving.GetListSavingRequest;
import com.mbc.mobileapp.rest.saving.GetListSavingResponse;
import com.mbc.mobileapp.rest.saving.campaign.CampaignSavingReponse;
import com.mbc.mobileapp.rest.saving.campaign.CampaignSavingRequest;
import com.mbc.mobileapp.rest.saving.close.DepositCloseRequest;
import com.mbc.mobileapp.rest.saving.close.DepositCloseResponse;
import com.mbc.mobileapp.rest.saving.close.ValidateDepositClosureRequest;
import com.mbc.mobileapp.rest.saving.close.ValidateDepositClosureResponse;
import com.mbc.mobileapp.rest.saving.cob.CheckCoBRequest;
import com.mbc.mobileapp.rest.saving.cob.CheckCoBResponse;
import com.mbc.mobileapp.rest.saving.interest.InterestRequest;
import com.mbc.mobileapp.rest.saving.interest.InterestResponseV2;
import com.mbc.mobileapp.rest.saving.open.OpenSavingRequest;
import com.mbc.mobileapp.rest.saving.open.OpenSavingResponse;
import com.mbc.mobileapp.rest.saving.open.ValidateSavingRequest;
import com.mbc.mobileapp.rest.saving.open.ValidateSavingResponse;
import com.mbc.mobileapp.rest.saving.topup.TopUpSavingRequest;
import com.mbc.mobileapp.rest.saving.topup.TopUpSavingResponse;
import com.mbc.mobileapp.rest.saving.topup.ValidateTopUpSavingRequest;
import com.mbc.mobileapp.rest.saving.topup.ValidateTopUpSavingResponse;
import com.mbc.mobileapp.service.base.AccountService;
import com.mbc.mobileapp.service.base.SavingAccountService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.Validator;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/saving")
public class SavingController extends BaseController {

    public SavingController(Validator validator) {
        super(validator);
    }

    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    @Autowired
    private SavingAccountService savingAccountService;

    @RequestMapping(value = "/account/list", method = RequestMethod.POST, produces = "application/json")
    public AccountSavingResponse getSavingAccount(@RequestBody AccountSavingRequest param,
                                                  HttpServletRequest requestClient) throws Exception {

        AccountSavingResponse resp = new AccountSavingResponse();
//        AccountSavingRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;
//        if (dynKeyEnabled) {
//
//            DynamicKeyResponse<AccountSavingRequest> dynResponse = dynDecryptData1(dynRequest, AccountSavingRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), AccountSavingRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        AppLog.info("[Get Saving Account List] input data: " + JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();

                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setSourceAccountNumber(param.getAccountId());
                    request.setAccountType(param.getAccountTypes());

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    // accountService = new AccountService();
                    resp = savingAccountService.getSavingAccount(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        AppLog.info("[Get Saving Account List] output data: " + JSON.stringify(resp));
        return resp;
    }

    @RequestMapping(value = "/campaign/list", method = RequestMethod.POST, produces = "application/json")
    public CampaignSavingReponse getCampaignSaving(@RequestBody CampaignSavingRequest param,
                                                   HttpServletRequest requestClient) throws Exception {
        CampaignSavingReponse resp = new CampaignSavingReponse();
        com.mbc.common.validator.base.Validator.Result result = null;

//        if (dynKeyEnabled) {
//
//            DynamicKeyResponse<AccountSavingRequest> dynResponse = dynDecryptData1(dynRequest, AccountSavingRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), AccountSavingRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK GET LIST CAMPAIGN SAVING] INPUT DATA: {}", JSON.stringify(param));
        if (param != null) {
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
                    request.setPartnerSdk(param.getPartner());

                    resp = savingAccountService.getCampaignSaving(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[SDK GET LIST CAMPAIGN SAVING] OUPUT DATA: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API Lấy danh sách sản phẩm tiết kiệm")
    @PostMapping("/get-product-saving")
    public GetListSavingResponse getListProductSaving(@Valid @RequestBody GetListSavingRequest param,
                                                      HttpServletRequest requestClient) {
        GetListSavingResponse resp = new GetListSavingResponse();
//        GetListSavingRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<GetListSavingRequest> dynResponse = dynDecryptData1(dynRequest, GetListSavingRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), GetListSavingRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[GET LIST PRODUCT SAVING] INPUT DATA: {}", JSON.stringify(param));
        if (param != null) {
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
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.getProductSaving(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[GET LIST PRODUCT SAVING] OUTPUT DATA: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API lấy lãi suất")
    @PostMapping("/interest")
    public InterestResponseV2 getInterest(@Valid @RequestBody InterestRequest param,
                                            HttpServletRequest requestClient) {

        InterestResponseV2 resp = new InterestResponseV2();
        com.mbc.common.validator.base.Validator.Result result;
//        InterestRequest param;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<InterestRequest> dynResponse = dynDecryptData1(dynRequest, InterestRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), InterestRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SAVING GET INTEREST] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
//                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    request.setInterestRequest(param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.getInterest(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[SAVING GET INTEREST] output data: {}", JSON.stringify(resp));
        return resp;
    }


    @ApiOperation("API validate open saving")
    @PostMapping("/validate")
    public ValidateSavingResponse validate(@Valid @RequestBody ValidateSavingRequest param,
                                           HttpServletRequest requestClient) {

        ValidateSavingResponse resp = new ValidateSavingResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        ValidateSavingRequest param;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<ValidateSavingRequest> dynResponse = dynDecryptData1(dynRequest, ValidateSavingRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), ValidateSavingRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SAVING VALIDATED] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setSavingInfo(param.getSavingInfo());
                request.setSavingProductCode(param.getSavingInfo().getProductCode());

                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.validate(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[SAVING VALIDATED] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API open saving")
    @PostMapping("/open")
    public OpenSavingResponse open(@Valid @RequestBody OpenSavingRequest param,
                                   HttpServletRequest requestClient) {

        OpenSavingResponse resp = new OpenSavingResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        OpenSavingRequest param;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<OpenSavingRequest> dynResponse = dynDecryptData1(dynRequest, OpenSavingRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), OpenSavingRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SAVING DEPOSIT OPEN] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setTransId(param.getTransId());
                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.open(request, cust, param.getTokenOTP());
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[SAVING DEPOSIT OPEN] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API validate saving closure")
    @PostMapping("/close-validate")
    public ValidateDepositClosureResponse closureValidate(@Valid @RequestBody ValidateDepositClosureRequest param,
                                                          HttpServletRequest requestClient) {
//        AppLog.info(requestClient.getRequestURI() + "-" + dynRequest.toString());
        ValidateDepositClosureResponse resp = new ValidateDepositClosureResponse();
//        ValidateDepositClosureRequest param;
        com.mbc.common.validator.base.Validator.Result result;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<ValidateDepositClosureRequest> dynResponse = dynDecryptData1(dynRequest, ValidateDepositClosureRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), ValidateDepositClosureRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SAVING CLOSE VALIDATED] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setDepositClosureInfo(param.getDepositClosureInfo());
//                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.depositClosureValidate(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[SAVING CLOSE VALIDATED] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API saving fixed deposit closure")
    @PostMapping("/close")
    public DepositCloseResponse closure(@Valid @RequestBody DepositCloseRequest param,
                                        HttpServletRequest requestClient) {

        DepositCloseResponse resp = new DepositCloseResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        DepositCloseRequest param;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<DepositCloseRequest> dynResponse = dynDecryptData1(dynRequest, DepositCloseRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), DepositCloseRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SAVING DEPOSIT CLOSED] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setTransId(param.getTransId());
//                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.depositClosure(request, cust, param.getTokenOTP());
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[SAVING DEPOSIT CLOSED] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API check close of business (cob)")
    @PostMapping("/check-cob")
    public CheckCoBResponse checkCoB(@Valid @RequestBody CheckCoBRequest param,
                                     HttpServletRequest requestClient) {

        CheckCoBResponse resp = new CheckCoBResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        CheckCoBRequest param;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<CheckCoBRequest> dynResponse = dynDecryptData1(dynRequest, CheckCoBRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), CheckCoBRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SAVING CHECK COB] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FIXED_DEPOSIT);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.checkCob(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[SAVING CHECK COB] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @RequestMapping(value = "/account/detail", method = RequestMethod.POST, produces = "application/json")
    public AccountSavingResponse getDetailSavingAccount(@RequestBody AccountSavingRequest param,
                                                  HttpServletRequest requestClient) throws Exception {
        AccountSavingResponse resp = new AccountSavingResponse();
//        AccountSavingRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;

//        if (dynKeyEnabled) {
//
//            DynamicKeyResponse<AccountSavingRequest> dynResponse = dynDecryptData1(dynRequest, AccountSavingRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), AccountSavingRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[GET DETAIL SAVING ACCOUNT] INPUT DATA: {}", JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();

                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setSourceAccountNumber(param.getAccountId());
                    request.setAccountType(param.getAccountTypes());

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    // accountService = new AccountService();
                    resp = savingAccountService.getDetailSavingAccount(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[GET DETAIL SAVING ACCOUNT] OUPUT DATA: {}", JSON.stringify(resp));
        return resp;
    }


    @ApiOperation("API validate topup saving")
    @PostMapping("/topup-validate")
    public ValidateTopUpSavingResponse validateTopUpFlexiTerm(@Valid @RequestBody ValidateTopUpSavingRequest param ,
                                                              HttpServletRequest requestClient) {
        ValidateTopUpSavingResponse resp = new ValidateTopUpSavingResponse();


//        ValidateTopUpSavingDepositRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<ValidateTopUpSavingDepositRequest> dynResponse = dynDecryptData1(dynRequest, ValidateTopUpSavingDepositRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), ValidateTopUpSavingDepositRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK SAVING FLEXI TOPUP VALIDATE] INPUT DATA: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setTopUpSavingInfo(param.getTopUpSavingInfo());
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM);
                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.validateTopUp(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[SDK SAVING FLEXI TOPUP VALIDATE] OUTPUT DATA: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API Execute TopUp ")
    @PostMapping("/topup")
    public TopUpSavingResponse topUpFlexiTerm(@Valid @RequestBody TopUpSavingRequest param,
                                              HttpServletRequest requestClient) {
        TopUpSavingResponse resp = new TopUpSavingResponse();

//        TopUpSavingDepositRequest param = null;
        com.mbc.common.validator.base.Validator.Result result  = null;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<TopUpSavingDepositRequest> dynResponse = dynDecryptData1(dynRequest, TopUpSavingDepositRequest.class);
//            param = dynResponse.getData();
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), TopUpSavingDepositRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK SAVING FLEXI TOPUP ] INPUT DATA: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                request.setSrvcCdCheck(Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM);
                request.setSrvcCd(Constant.SrvcCd.SRVC_SAVING_FLEXI_TERM);
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    request.setTransId(param.getTransId());

                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = savingAccountService.executeTopUp(request, cust, param.getTokenOTP());
                }
            }
            resp.setRefNo(param.getRefNo());

        }
        log.info("[SDK SAVING FLEXI TOPUP ] OUTPUT DATA: {}", JSON.stringify(resp));
        return resp;
    }
}
