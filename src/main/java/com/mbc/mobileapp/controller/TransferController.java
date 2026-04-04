package com.mbc.mobileapp.controller;

import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.Constant;
import com.mbc.common.util.JSON;
import com.mbc.mobileapp.constant.CommonServiceConstant;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.transfer.*;
import com.mbc.mobileapp.rest.transfer.banklist.ListBankCiftpResponse;
import com.mbc.mobileapp.rest.transfer.banklist.ListBankRequest;
import com.mbc.mobileapp.rest.transfer.ciftp.AccountInquiryCasaRequest;
import com.mbc.mobileapp.rest.transfer.ciftp.AccountInquiryResponse;
import com.mbc.mobileapp.rest.transfer.ciftp.wallet.AccountInquiryWalletRequest;
import com.mbc.mobileapp.rest.transfer.khqr.*;
import com.mbc.mobileapp.service.base.TransferService;
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
@RequestMapping("/transfer")
public class TransferController extends BaseController {
    public TransferController(Validator validator) {
        super(validator);
    }

    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    @Autowired
    private TransferService transferService;


    @ApiOperation("API check transfer")
    @PostMapping("/valid-trans-info")
    public TransInfoResponse validateInhouseTransfer(@Valid @RequestBody TransInfoRequest param,
                                                               HttpServletRequest requestClient) throws Exception {

        TransInfoResponse resp = new TransInfoResponse();
//        TransInfoRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<InhouseTransferRequest> dynResponse = dynDecryptData1(dynRequest, InhouseTransferRequest.class);
//
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), InhouseTransferRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Valid Transfer] input data: {}", JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            }
            else {
                CommonServiceRequest request = new CommonServiceRequest();

                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    param.getTransInfo().setBranchCode(Constant.BRANCH_CODE_HO);
                    request.setTransInfo(param.getTransInfo());
                    request.setSourceAccountNumber(param.getTransInfo().getDebitAcctNo());
                    request.setDestAccountNumber(param.getTransInfo().getCreditAcctNo());
                    request.setDestAccountCurrency(param.getTransInfo().getCreditCurrency());

                    if(Constant.SrvcCd.SRVC_TRANS_CIFTP_CASA.equals(param.getSrvcCd())){
                        request.setCiftpTransferType(CommonServiceConstant.Service.CASA_TO_CASA.name());
                        request.setAcctNo(param.getTransInfo().getCreditAcctNo());
                        request.setParticipantCode(param.getTransInfo().getDestBankPartiCode());
                    }

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);

                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    resp = transferService.validateTransfer(request, cust);
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[Valid Transfer] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API execute transfer")
    @PostMapping("/execute-transfer")
    public MakeTransferResponse executeTransfer(@Valid @RequestBody MakeTransferRequest param,
                                                                 HttpServletRequest requestClient) throws Exception {

        MakeTransferResponse resp = new MakeTransferResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
//        MakeTransferRequest param = null;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<MakeTransferRequest> dynResponse = dynDecryptData1(dynRequest, MakeTransferRequest.class);
//
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), MakeTransferRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[Execute Transfer] input data: {}", JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            }
            else {

                CommonServiceRequest request = new CommonServiceRequest();

                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setTransId(param.getTransInfo().getTransId());
                    request.setCiftpSettlement(param.getTransInfo().getSettlement());

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);

                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    // accountService = new AccountService();
                    resp = transferService.makeTransfer(request, cust, param.getTokenOTP());
                }
            }
            resp.setRefNo(param.getRefNo());
        }
        log.info("[Execute Transfer] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API query bank list ciftp")
    @PostMapping("/ciftp/bank-list")
    public ListBankCiftpResponse getListBankCiftp(@Valid @RequestBody ListBankRequest param,
                                                  HttpServletRequest requestClient) {

        ListBankCiftpResponse resp = new ListBankCiftpResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
        // validation
        log.info("[CIFTP Get List Bank] input data: {}", JSON.stringify(param));
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        }
        else {
            CommonServiceRequest request = new CommonServiceRequest();
            CustInfo cust = getCustFromSession(param.getSessionId());
            if (cust != null) {
                // Common param
                request = (CommonServiceRequest) setBase(request, param);
                Principal principal = requestClient.getUserPrincipal();
                request.setPartnerId(principal.getName());
                request.setPartnerSdk(param.getPartner());

                resp = transferService.getListBankCiftp(request, cust);
            }
        }

        resp.setRefNo(param.getRefNo());
        log.info("[CIFTP Get List Bank] output data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API check account information to casa")
    @PostMapping("/ciftp/casa/check-info-account")
    public AccountInquiryResponse checkInfoAccountToCasa(@Valid @RequestBody AccountInquiryCasaRequest param,
                                                         HttpServletRequest requestClient) throws Exception {


        AccountInquiryResponse resp = new AccountInquiryResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
//        AccountInquiryCasaRequest param = null;
//
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<AccountInquiryCasaRequest> dynResponse =
//                    dynDecryptData1(dynRequest, AccountInquiryCasaRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        }
//        else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), AccountInquiryCasaRequest.class);
//            if (param == null) {
//                result =
//                        new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK CIFTP casa check info account] input data: {} ", JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            }
            else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setAcctNo(param.getAccountNumber());
                    request.setParticipantCode(param.getParticipantCode());
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_TRANS_CIFTP_CASA);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = transferService.ciftpAccountInquiryCasaService(request, cust);

                }
            }

            resp.setRefNo(param.getRefNo());
        }

        log.info("[SDK CIFTP Casa check account info] output data: {} ", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API check account information to casa")
    @PostMapping("/khqr/check-info")
    public KHQRCheckInfoResponse checkInfoAccountToWallet(@Valid @RequestBody KHQRCheckInfoRequest param,
                                                          HttpServletRequest requestClient) throws Exception {


        KHQRCheckInfoResponse resp = new KHQRCheckInfoResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
//        AccountInquiryWalletRequest param = null;
//
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<AccountInquiryWalletRequest> dynResponse =
//                    dynDecryptData1(dynRequest, AccountInquiryWalletRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        }
//        else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), AccountInquiryWalletRequest.class);
//            if (param == null) {
//                result =
//                        new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK CIFTP casa check info account] input data: {} ", JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            }
            else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setPayloadQr(param.getPayloadQr());
                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_TRANS_CIFTP_KHQR);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());
                    request.setPartnerSdk(param.getPartner());

                    resp = transferService.checkInfoKHQRTransfer(request, cust);

                }
            }

            resp.setRefNo(param.getRefNo());
        }

        log.info("[SDK CIFTP Casa check account info] output data: {} ", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API check tranfer infomation to wallet")
    @PostMapping("/khqr/valid-trans-info")
    public KHQRTransferResponse validTransInfoToWallet(@Valid @RequestBody KHQRTransferRequest param,
                                                       HttpServletRequest requestClient) throws Exception {
        KHQRTransferResponse resp = new KHQRTransferResponse();
//        KHQRTransferRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<KHQRTransferRequest> dynResponse = dynDecryptData1(dynRequest, KHQRTransferRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//
//            }
//        }
//        else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), KHQRTransferRequest.class);
//            if (param == null) {
//                result =
//                        new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK CIFTP Wallet valid trans] input data: {} ", JSON.stringify(param));
        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            }
            else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    param.getTransInfo().setBranchCode(Constant.BRANCH_CODE_HO);
                    request.setTransInfo(param.getTransInfo());
                    request.setSourceAccountNumber(param.getTransInfo().getDebitAcctNo());
                    request.setDestAccountNumber(param.getTransInfo().getCreditAcctNo());
                    request.setDestAccountCurrency(param.getTransInfo().getCreditCurrency());

                    request.setAccountId(param.getTransInfo().getCreditAcctNo());
//                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_TRANS_CIFTP_WALLET);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = transferService.validKHQRTransfer(request, cust);

                }
            }
            resp.setRefNo(param.getRefNo());
        }

        log.info("[SDK CIFTP Wallet valid trans] out data: {}", JSON.stringify(resp));
        return resp;
    }

    @ApiOperation("API execute tranfer infomation to wallet")
    @PostMapping("khqr/execute-transfer")
    public KHQRMakeTransferResponse executeTransInfoToWallet(@Valid @RequestBody KHQRMakeTransferRequest param,
                                                             HttpServletRequest requestClient) throws Exception {

        KHQRMakeTransferResponse resp = new KHQRMakeTransferResponse();
//        KHQRMakeTransferRequest param = null;
        com.mbc.common.validator.base.Validator.Result result = null;

//        if (dynKeyEnabled) {
//            DynamicKeyResponse<KHQRMakeTransferRequest> dynResponse =
//                    dynDecryptData1(dynRequest, KHQRMakeTransferRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        }
//        else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), KHQRMakeTransferRequest.class);
//            if (param == null) {
//                result =
//                        new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false, ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }

        log.info("[SDK CIFTP KHQR execute trans] input data: {}" , JSON.stringify(param));

        if (param != null) {
            // validation
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            }
            else {
                CommonServiceRequest request = new CommonServiceRequest();
                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setTransId(param.getTransInfo().getTransId());
//                    request.setSrvcCdCheck(Constant.SrvcCd.SRVC_TRANS_CIFTP_WALLET);

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    request.setPartnerSdk(param.getPartner());

                    resp = transferService.executeKHQRTransfer(request, cust, param.getTokenOTP());

                }
            }

            resp.setRefNo(param.getRefNo());
        }
        log.info("[SDK CIFTP KHQR execute trans] out data: {}" , JSON.stringify(resp));
        return resp;

    }

}
