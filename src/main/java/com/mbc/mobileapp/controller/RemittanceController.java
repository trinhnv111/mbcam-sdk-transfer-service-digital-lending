package com.mbc.mobileapp.controller;

import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.remittance.addr.RemittanceAddressRequest;
import com.mbc.mobileapp.rest.remittance.addr.RemittanceAddressResponse;
import com.mbc.mobileapp.rest.remittance.banklist.BankListRequest;
import com.mbc.mobileapp.rest.remittance.banklist.BankListResponse;
import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishRequest;
import com.mbc.mobileapp.rest.remittance.finish.MakeTransferFinishResponse;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountNameRequest;
import com.mbc.mobileapp.rest.remittance.getaccountname.GetAccountNameResponse;
import com.mbc.mobileapp.rest.remittance.init.MakeTransferInitRequest;
import com.mbc.mobileapp.rest.remittance.init.MakeTransferInitResponse;
import com.mbc.mobileapp.rest.remittance.promocode.GetPromoCodeRequest;
import com.mbc.mobileapp.rest.remittance.promocode.GetPromoCodeResponse;
import com.mbc.mobileapp.service.base.RemittanceService;
import io.swagger.annotations.ApiOperation;
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
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/remittance")
public class RemittanceController extends BaseController {

    public RemittanceController(Validator validator) {
        super(validator);
    }

    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    @Autowired
    private RemittanceService remittanceService;

    @ApiOperation("API init transfer ")
    @PostMapping("/validate")
    public MakeTransferInitResponse validate(@Valid @RequestBody MakeTransferInitRequest param,
                                             HttpServletRequest requestClient) throws Exception {

        MakeTransferInitResponse resp = new MakeTransferInitResponse();
//        MakeTransferInitRequest param ;
        com.mbc.common.validator.base.Validator.Result result;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<MakeTransferInitRequest> dynResponse =
//                    dynDecryptData1(dynRequest, MakeTransferInitRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), MakeTransferInitRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Remittance Valid Transfer] input data: {} " + JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    param.getInitMakeTransferInfo().setDescription(cust.getNm() + " transfer");

                    request.setInitMakeTransferInfo(param.getInitMakeTransferInfo());
                    request.setSourceAccountNumber(param.getInitMakeTransferInfo().getDebitAcctNo());
                    request.setDestAccountNumber(param.getInitMakeTransferInfo().getCreditAcctNo());
                    request.setDestAccountCurrency(param.getInitMakeTransferInfo().getCreditCurrency());
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER);

//                    GetAccountNameRequest benAccount = new GetAccountNameRequest();
//                    benAccount.setAccountNo(param.getInitMakeTransferInfo().getCreditAcctNo());
//                    benAccount.setBenBank(param.getInitMakeTransferInfo().getDestBankCode());
//                    request.setGetAccountNameRequest(benAccount);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = remittanceService.validate(request, cust);

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[Remittance Valid Transfer] output data: {} " + JSON.stringify(resp));
        return resp;
    }


    @ApiOperation("API execute transfer ")
    @PostMapping("/execute-transfer")
    public MakeTransferFinishResponse finish(@Valid @RequestBody MakeTransferFinishRequest param,
                                             HttpServletRequest requestClient) throws Exception {

        MakeTransferFinishResponse resp = new MakeTransferFinishResponse();
//        MakeTransferFinishRequest param ;
        com.mbc.common.validator.base.Validator.Result result;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<MakeTransferFinishRequest> dynResponse =
//                    dynDecryptData1(dynRequest, MakeTransferFinishRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), MakeTransferFinishRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Remittance Execute Transfer] input data: {} " + JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setTransId(param.getTransId());
                    request.setMakeTransferFinishRequest(param);
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = remittanceService.finish(request, cust,param.getTokenOTP());

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[Remittance Execute Transfer] output data: {} " + JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API  bank list ")
    @PostMapping("/bank-list")
    public BankListResponse getBankList(@Valid @RequestBody BankListRequest param,
                                        HttpServletRequest requestClient) throws Exception {

        BankListResponse resp = new BankListResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        BankListRequest param;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<BankListRequest> dynResponse =
//                    dynDecryptData1(dynRequest, BankListRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), BankListRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Remittance Get  Bank List] input data: {} " + JSON.stringify(param));
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
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER);
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = remittanceService.getBankList(request, cust);

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[Remittance Get  Bank List] output data: {} " + JSON.stringify(resp));
        return resp;
    }


    @ApiOperation("API  get Account Name ")
    @PostMapping("/get-account-name")
    public GetAccountNameResponse getAccountName(@Valid @RequestBody GetAccountNameRequest param,
                                                 HttpServletRequest requestClient) throws Exception {

        GetAccountNameResponse resp = new GetAccountNameResponse();
        com.mbc.common.validator.base.Validator.Result result;
//        GetAccountNameRequest param;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<GetAccountNameRequest> dynResponse =
//                    dynDecryptData1(dynRequest, GetAccountNameRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), GetAccountNameRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Remittance Get Account Name] input data: {} " + JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER);
                    request.setGetAccountNameRequest(param);
                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = remittanceService.getAccountName(request, cust);

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[Remittance Get Account Name] output data: {} " + JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API Get Adrress VN")
    @PostMapping("/get-address-vn")
    public RemittanceAddressResponse getAddressVn(@Valid @RequestBody RemittanceAddressRequest param,
                                                  HttpServletRequest requestClient) throws Exception {

        RemittanceAddressResponse resp = new RemittanceAddressResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
//        RemittanceAddressRequest param = null;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<RemittanceAddressRequest> dynResponse =
//                    dynDecryptData1(dynRequest, RemittanceAddressRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), RemittanceAddressRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Remittance Get Addess VN] input data: {} " + JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER);
                    request.setType(param.getType());
                    request.setCode(param.getCode());

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = remittanceService.getAddressVn(request, cust);

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[Remittance Get Addess VN] output data: {} " + JSON.stringify(resp));
        return resp;
    }


    @ApiOperation("API init transfer ")
    @PostMapping("/get-promo-code")
    public GetPromoCodeResponse getPromoCode(@Valid @RequestBody GetPromoCodeRequest param,
                                         HttpServletRequest requestClient) throws Exception {

        GetPromoCodeResponse resp = new GetPromoCodeResponse();
//        MakeTransferInitRequest param ;
        com.mbc.common.validator.base.Validator.Result result;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<MakeTransferInitRequest> dynResponse =
//                    dynDecryptData1(dynRequest, MakeTransferInitRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), MakeTransferInitRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Remittance Get Promo Code] input data: {} " + JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setPromoCode(param.getPromoCode());
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_INTERNATIONAL_TRANSFER);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = remittanceService.getPromoCode(request, cust);

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[Remittance Get Promo Code] output data: {} " + JSON.stringify(resp));
        return resp;
    }


}
