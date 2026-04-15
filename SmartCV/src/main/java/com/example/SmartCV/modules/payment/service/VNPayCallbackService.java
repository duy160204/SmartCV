package com.example.SmartCV.modules.payment.service;

import java.time.LocalDateTime;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.config.VnpayConfig;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.admin.service.AdminSubscriptionRequestService;
import com.example.SmartCV.modules.payment.util.VnpaySignatureUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackService implements PaymentCallbackService {

    private final VnpayConfig vnpayConfig;
    private final PaymentTransactionRepository paymentRepo;
    private final AdminSubscriptionRequestService adminSubscriptionRequestService;

    @Override
    @Transactional(readOnly = true)
    public void handleVNPayReturn(HttpServletRequest request) {
        Map<String, String> params = VnpaySignatureUtil.extractRawParams(request);
        String txnRef = params.get("vnp_TxnRef");
        String ipAddress = params.getOrDefault("vnp_IpAddr", "unknown");

        log.info("[VNPAY][RETURN] Displaying return page for txnRef={} IP={}", txnRef, ipAddress);

        boolean isValid = VnpaySignatureUtil.verifySignature(params, vnpayConfig.getHashSecret().trim());
        if (!isValid) {
            log.error("[VNPAY][RETURN] Signature invalid! txnRef={}", txnRef);
            return;
        }

        log.info("[VNPAY][RETURN] Signature valid! Frontend will display status based on vnp_ResponseCode.");
    }

    @Override
    @Transactional
    public boolean handleVNPayIpn(HttpServletRequest request) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("ipn_debug.log", true))) {
            writer.println("================ IPN WEBHOOK RECEIVED ================");
            writer.println("Time: " + LocalDateTime.now());
            log.info("================ IPN WEBHOOK RECEIVED ================");

            // Step 1: Print full raw query string
            writer.println("[VNPAY DEBUG] Step 1 - Raw QueryString: " + request.getQueryString());
            log.info("[VNPAY DEBUG] Step 1 - Raw QueryString: {}", request.getQueryString());

            // Step 2: Extract params
            Map<String, String> params = VnpaySignatureUtil.extractRawParams(request);

            // Step 3: Log critical fields
            String txnRef = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String vnpAmountStr = params.get("vnp_Amount");
            String vnpSecureHash = params.get("vnp_SecureHash");

            writer.println("[VNPAY DEBUG] Step 2 & 3 - Extracted Params: " + txnRef + ", " + vnpAmountStr + ", " + responseCode);
            log.info("[VNPAY DEBUG] Step 2 & 3 - Extracted Params:");
            log.info("   vnp_TxnRef       : {}", txnRef);
            log.info("   vnp_Amount       : {}", vnpAmountStr);
            log.info("   vnp_ResponseCode : {}", responseCode);
            log.info("   vnp_SecureHash   : {}", vnpSecureHash);

            if (txnRef == null || responseCode == null || vnpAmountStr == null) {
                log.error(
                        "[VNPAY DEBUG] FAILED: Missing required params (txnRef, responseCode, or vnpAmountStr is null)");
                return false;
            }

            log.error("🔥 [IPN VERIFY START] txnRef={}", txnRef);
            boolean isValid = VnpaySignatureUtil.verifySignature(params, vnpayConfig.getHashSecret().trim());
            log.error("🔥 [IPN VERIFY RESULT] valid={}", isValid);
            if (!isValid) {
                log.error("[VNPAY DEBUG] FAILED: Signature mismatch. Secret used for hashing="
                        + vnpayConfig.getHashSecret().trim().substring(0, 4) + "...");
                return false;
            }
            log.info("[VNPAY DEBUG] SUCCESS: Signature is absolutely valid.");

            // Step 5: Validate transaction existence
            writer.println("[VNPAY DEBUG] Step 5 - Validating Transaction in DB for " + txnRef);
            log.info("[VNPAY DEBUG] Step 5 - Validating Transaction in DB...");
            var optionalTx = paymentRepo.findByTransactionCode(txnRef);
            if (optionalTx.isEmpty()) {
                log.error("[VNPAY DEBUG] FAILED: TRANSACTION NOT FOUND in database for txnRef={}", txnRef);
                return false;
            }
            PaymentTransaction tx = optionalTx.get();
            log.info("[VNPAY DEBUG] SUCCESS: Transaction found in DB. DB amount={}", tx.getAmount());

            // Step 6: Validate amount
            log.info("[VNPAY DEBUG] Step 6 - Validating Amount...");
            long vnpAmount;
            try {
                vnpAmount = Long.parseLong(vnpAmountStr);
            } catch (NumberFormatException e) {
                log.error("[VNPAY DEBUG] FAILED: Amount parse error! raw={}", vnpAmountStr);
                return false;
            }

            long expectedAmount = tx.getAmount() * 100L;
            if (vnpAmount != expectedAmount) {
                log.error("[VNPAY DEBUG] FAILED: Amount mismatch! txnRef={}, DB AMOUNT (cents)={}, VNPay AMOUNT={}",
                        txnRef, expectedAmount, vnpAmount);
                return false;
            }
            log.info("[VNPAY DEBUG] SUCCESS: Amount perfectly matches.");

            // Step 7: Validate idempotency
            log.info("[VNPAY DEBUG] Step 7 - Validating Idempotency...");
            if (tx.isSuccess()) {
                log.info(
                        "[VNPAY DEBUG] SUCCESS (ALREADY PROCESSED): Transaction already SUCCESS in DB. Skipping exact duplicate.");
                return true;
            }
            log.info("[VNPAY DEBUG] SUCCESS: Transaction is PENDING/new, proceeding to update.");

            // Step 8: Validate responseCode
            log.info("[VNPAY DEBUG] Step 8 - Validating vnp_ResponseCode...");
            if ("00".equals(responseCode)) {
                log.info("[VNPAY DEBUG] SUCCESS: Response code is 00 (Payment successful). Updating DB...");
                log.error("🔥 [IPN DB UPDATE START]");
                int rows = paymentRepo.updateStatusAtomically(txnRef, PaymentStatus.PENDING, PaymentStatus.SUCCESS,
                        LocalDateTime.now());
                log.error("🔥 [IPN DB UPDATE SUCCESS]");
            writer.println("   rows updated: " + rows);
            if (rows == 0) {
                    log.info("[VNPAY DEBUG] SUCCESS (ALREADY PROCESSED CONCURRENTLY): Duplicate success IPN ignored.");
                    return true;
                }

                log.error("🔥 [IPN SUBSCRIPTION CREATE]");
                PaymentTransaction fresh = paymentRepo.findByTransactionCode(txnRef).orElse(tx);
                adminSubscriptionRequestService.createFromPaymentSuccess(fresh);
            } else {
                log.warn("[VNPAY DEBUG] FAILED: Payment unsuccessful. ResponseCode={}. Marking FAILED in DB.",
                        responseCode);
                paymentRepo.updateStatusAtomically(txnRef, PaymentStatus.PENDING, PaymentStatus.FAILED, null);
                // Return true so VNPay knows we processed the failure and doesn't retry
            }

            writer.println("================ IPN WEBHOOK COMPLETE ================");
            log.info("================ IPN WEBHOOK COMPLETE ================ ");
            return true;

        } catch (Exception e) {
            log.error("[VNPAY DEBUG] FAILED: Unexpected fatal IPN error: ", e);
            return false;
        }
    }
}
