package com.mbc.mobileapp.controller;

import com.mbc.common.controller.BaseController;
import com.mbc.common.object.CustInfo;
import com.mbc.common.util.JSON;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.beneficiary.BeneficiaryRequest;
import com.mbc.mobileapp.rest.beneficiary.BeneficiaryResponse;
import com.mbc.mobileapp.rest.beneficiary.SaveBeneficiaryRequest;
import com.mbc.mobileapp.rest.beneficiary.SaveBeneficiaryResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRMakeTransferRequest;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRMakeTransferResponse;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRTransferRequest;
import com.mbc.mobileapp.rest.transfer.khqr.KHQRTransferResponse;
import com.mbc.mobileapp.service.base.BeneficiaryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/beneficiary")
public class BeneficiaryController extends BaseController {
    public BeneficiaryController(Validator validator) {
        super(validator);
    }

    @Value("${dynk.enabled}")
    private boolean dynKeyEnabled;

    @Autowired
    private BeneficiaryService beneficiaryService;


    @ApiOperation("API list beneficiary")
    @PostMapping("/list")
    public BeneficiaryResponse getBeneficiaryList(@RequestBody BeneficiaryRequest param,
                                                  HttpServletRequest requestClient) {

        BeneficiaryResponse resp = new BeneficiaryResponse();
        com.mbc.common.validator.base.Validator.Result result = null;
//        BeneficiaryRequest param =  null;
//        if (dynKeyEnabled) {
//            DynamicKeyResponse<BeneficiaryRequest> dynResponse = dynDecryptData1(dynRequest, BeneficiaryRequest.class);
//            param = dynResponse.getData();
//
//            if (param == null) {
//                result = new SimpleResult(dynResponse.getDynResponse().getM_statusCode(), false,
//                        ResponseCode.DYNKEY_DECRYPT_ERROR.getCode());
//                resp.setResult(result);
//            }
//        } else {
//            param = mapDataRequestBody(dynRequest.getDataEncrypt(), BeneficiaryRequest.class);
//            if (param == null) {
//                result = new SimpleResult(ResponseCode.INVALID_INPUT.getDesc(), false,
//                        ResponseCode.INVALID_INPUT.getCode());
//                resp.setResult(result);
//            }
//        }
        // validation

        log.info("[SDK GET LIST BENEFICIARY] input data: {}", JSON.stringify(param));
        if (param != null) {
            result = validate(param);
            if (!result.isOk()) {
                resp.setResult(result);
            } else {
                CommonServiceRequest request = new CommonServiceRequest();

                CustInfo cust = getCustFromSession(param.getSessionId());
                if (cust != null) {
                    request.setUserId(cust.getUserId());
                    // request.setAccountType(param.getAccountTypes());

                    // Common param
                    request = (CommonServiceRequest) setBase(request, param);
                    Principal principal = requestClient.getUserPrincipal();
                    request.setPartnerId(principal.getName());
                    resp = beneficiaryService.getBeneficiaryList(request, cust);
                }
            }

            resp.setRefNo(param.getRefNo());

        }
        log.info("[SDK GET LIST BENEFICIARY] output data: {}", JSON.stringify(resp));
        return resp;
    }


    @ApiOperation(value = "", notes = "transferType thêm type : Wallet,partnerCode =(BAKONG ,FAST,MBC, CIFTP) ")
    @RequestMapping(value = "/save-beneficiary", method = RequestMethod.POST, produces = "application/json")
    public SaveBeneficiaryResponse saveBeneficiary(@RequestBody SaveBeneficiaryRequest param,
                                                   HttpServletRequest requestClient) {

        SaveBeneficiaryResponse resp = new SaveBeneficiaryResponse();
        com.mbc.common.validator.base.Validator.Result result = null;


        log.info("[SDK SAVE BENEFICIARY] input data: {}", JSON.stringify(param));
        // validation
        result = validate(param);
        if (!result.isOk()) {
            resp.setResult(result);
        } else {
            CommonServiceRequest request = new CommonServiceRequest();

            CustInfo cust = getCustFromSession(param.getSessionId());
            if (cust != null) {
                request.setUserId(cust.getUserId());
                request.setBenAcctNo(param.getBeneInfo().getBenAcctNo());
                request.setBenAcctName(param.getBeneInfo().getBenAcctName());
                request.setBenBankName(param.getBeneInfo().getBenBankName());
                request.setBenBankCode(param.getBeneInfo().getBenBankCode());
                request.setSuggestName(param.getBeneInfo().getSuggestName());
                request.setBenBankMnemonic(param.getBeneInfo().getBenBankMnemonic());
                request.setBeneficiaryId(param.getBeneInfo().getBeneficiaryId());
                request.setCurrency(param.getBeneInfo().getCurrency());
                request.setTransType(param.getBeneInfo().getTransType());
                request.setType(param.getBeneInfo().getType());
                request.setPartnerCode(param.getBeneInfo().getPartner());

                // Common param
                request = (CommonServiceRequest) setBase(request, param);
                Principal principal = requestClient.getUserPrincipal();
                request.setPartnerId(principal.getName());
                resp = beneficiaryService.saveBeneficiary(request, cust);
            }
        }
        resp.setRefNo(param.getRefNo());
        log.info("[SDK SAVE BENEFICIARY] output data: {}", JSON.stringify(resp));
        return resp;
    }

}
