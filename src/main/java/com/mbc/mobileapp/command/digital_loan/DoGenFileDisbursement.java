package com.mbc.mobileapp.command.digital_loan;

import com.mbc.common.api.CallBirtService;
import com.mbc.common.bean.ProcessContext;
import com.mbc.common.bean.ResponseCode;
import com.mbc.common.entity.ComTransDtlLoanRegistration;
import com.mbc.common.il.base.ExecuteT24Output;
import com.mbc.common.object.CustInfo;
import com.mbc.common.repository.ComTransDtlLoanRegistrationRepo;
import com.mbc.common.services.il.loanorigination.*;
import com.mbc.common.util.Constant;
import com.mbc.common.validator.base.Validator;
import com.mbc.gateway.validator.result.SimpleResult;
import com.mbc.mobileapp.api.model.loanorigination.output.DoGenFileOutput;
import com.mbc.mobileapp.rest.bean.CommonServiceRequest;
import com.mbc.mobileapp.rest.bean.CommonServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.mbc.common.util.Constant.COM_STATUS_INT;

/**
 * Step: Tạo hợp đồng PDF (BIRT) và lưu lên FTS
 *
 * Flow: SDK BE → BIRT → FTS → (ECM) → docId
 *
 * Quy trình:
 *  1. Gọi BIRT service sinh file PDF hợp đồng giải ngân theo 2 template: ENG + KHR
 *  2. Upload từng file lên FTS server → lấy fileId
 *  3. Lưu fileId (docIdEng / docIdKhr) vào ComTransDtlLoanRegistration qua process()
 *
 * Input:
 *  - request.getTransId()       → ID của ComTransDtlLoanRegistration (từ /get-confirm)
 *  - custInfo.getHostCifId()    → mã CIF
 *
 * Output vào response:
 *  - response.setDoGenFileOutput(...)  → fileContentEng + fileContentKhr (base64 PDF)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DoGenFileDisbursement implements Command {

    private final CallBirtService callBirtService;
    private final ComTransDtlLoanRegistrationRepo comTransDtlLoanRegistrationRepo;

    private static final List<String> TEMPLATE_NAMES = List.of(
            "SALARY_ADVANCE_CONTRACT_ENG",
            "SALARY_ADVANCE_CONTRACT_KHR"
    );

    @Override
    public boolean execute(Context ctx) throws Exception {
        ProcessContext context = (ProcessContext) ctx;
        Validator.Result result = Validator.Result.OK;
        CommonServiceRequest request = (CommonServiceRequest) context.getRequest();
        CommonServiceResponse response = (CommonServiceResponse) context.getResponse();
        CustInfo custInfo = context.getCustomer();

        String custId = custInfo.getHostCifId();
        String transId = request.getTransId();

        FileContractInput input = new FileContractInput();
        ExecuteT24Output<ContractFileOutput> birtOutput = null;
        DoGenFileOutput doGenFileOutput = new DoGenFileOutput();
        String fileContent = null;
        SaveFileContractInput saveFileContractInput = new SaveFileContractInput();
        ExecuteT24Output<SaveFileContractOutput> ftsResponse = null;
        ParamInput paramInput = new ParamInput();
        String step = "GEN_FILE"; // bước lưu docId sau khi upload FTS thành công

        try {
            input.setAppCode("MOBILE.RETAIL");
            paramInput.setHostCifId(custId);
            input.setParams(paramInput);

            for (String templateName : TEMPLATE_NAMES) {
                input.setTemplateName(templateName);

                // ── Bước 1: Gọi BIRT gen PDF ──────────────────────────────────
                birtOutput = callBirtService.genFileContractLoan(input, custId, request.getRequestId());
                if (Objects.isNull(birtOutput)) {
                    log.error("[DoGenFileDisbursement] BIRT timeout - template:{}, requestId:{}", templateName, request.getRequestId());
                    result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                            ResponseCode.REQUEST_TIMEOUT.getCode());
                    context.setResult(result);
                    return !result.isOk();
                }

                if (Constant.CALL_MICROSERVICE_SUCCESS.equals(birtOutput.getStatus())) {
                    fileContent = birtOutput.getData().getFileContent();
                    if (Objects.isNull(fileContent)) {
                        log.error("[DoGenFileDisbursement] fileContent null - template:{}, requestId:{}", templateName, request.getRequestId());
                        result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                                ResponseCode.TRANSACTION_FAIL.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }

                    // ── Bước 2: Upload lên FTS ─────────────────────────────────
                    saveFileContractInput.setCustomerCode(custId);
                    saveFileContractInput.setFileContent(fileContent);
                    saveFileContractInput.setFileType(birtOutput.getData().getFileExtension());
                    String filename = custId + "_salary_advance_disbursement_"
                            + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".pdf";
                    saveFileContractInput.setFileName(filename);

                    /*
                     * TODO ECM: upload lên ECM
                     * step = "GEN_FILE";
                     * process("", transId, step, "");
                     */

                    ftsResponse = callBirtService.saveFileContractFts(saveFileContractInput, custId, request.getRequestId());

                    if (Objects.isNull(ftsResponse)) {
                        log.error("[DoGenFileDisbursement] FTS timeout - template:{}, requestId:{}", templateName, request.getRequestId());
                        result = new SimpleResult(ResponseCode.REQUEST_TIMEOUT.getDesc(), false,
                                ResponseCode.REQUEST_TIMEOUT.getCode());
                        context.setResult(result);
                        return !result.isOk();
                    }

                    if (Constant.CALL_MICROSERVICE_SUCCESS.equals(ftsResponse.getStatus())) {
                        if (Objects.isNull(ftsResponse.getData()) || Objects.isNull(ftsResponse.getData().getFileId())) {
                            log.error("[DoGenFileDisbursement] FTS fileId null - template:{}, requestId:{}", templateName, request.getRequestId());
                            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                                    ResponseCode.TRANSACTION_FAIL.getCode());
                            context.setResult(result);
                            return !result.isOk();
                        }

                        // ── Bước 3: Lưu docId vào DB ──────────────────────────
                        String fileId = ftsResponse.getData().getFileId();
                        log.info("[DoGenFileDisbursement] FTS OK - fileId:{}, template:{}", fileId, templateName);

                        switch (templateName) {
                            case "SALARY_ADVANCE_CONTRACT_ENG":
                                doGenFileOutput.setFileContentEng(fileContent);
                                process(fileId, transId, step, "ENG");
                                break;
                            case "SALARY_ADVANCE_CONTRACT_KHR":
                                doGenFileOutput.setFileContentKhr(fileContent);
                                process(fileId, transId, step, "KHR");
                                break;
                            default:
                                log.warn("[DoGenFileDisbursement] Unknown template: {}", templateName);
                                break;
                        }
                        doGenFileOutput.setTransId(transId);
                        response.setDoGenFileOutput(doGenFileOutput);
                    }
                }
            }

            process("", transId, "SAVE_FILE", "");
            log.info("[DoGenFileDisbursement] All templates done - requestId:{}", request.getRequestId());

        } catch (Exception e) {
            log.error("[DoGenFileDisbursement] Exception - requestId:{}, desc: ", request.getRequestId(), e);
            result = new SimpleResult(ResponseCode.TRANSACTION_FAIL.getDesc(), false,
                    ResponseCode.TRANSACTION_FAIL.getCode());
        }
        context.setResult(result);
        return !result.isOk();
    }

    /**
     * Lưu fileId (docIdEng / docIdKhr) và step vào ComTransDtlLoanRegistration
     */
    private void process(String fileId, String transId, String step, String type) {
        ComTransDtlLoanRegistration reg =
                comTransDtlLoanRegistrationRepo.findByIdAndStatus(transId, COM_STATUS_INT);
        if (reg == null) {
            log.warn("[DoGenFileDisbursement] Registration not found - transId:{}", transId);
            return;
        }
        if ("ENG".equals(type)) {
            reg.setDocIdEng(fileId);
        } else if ("KHR".equals(type)) {
            reg.setDocIdKhr(fileId);
        }
        reg.setStep(step);
        reg.setUpdatedAt(new Date());
        comTransDtlLoanRegistrationRepo.save(reg);
    }
}
