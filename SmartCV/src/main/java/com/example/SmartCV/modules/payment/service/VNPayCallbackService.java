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
    @Transactional
    public void handleVNPayReturn(HttpServletRequest request) {
        Map<String, String> params = VnpaySignatureUtil.extractRawParams(request);
        String txnRef = params.get("vnp_TxnRef");
        String ipAddress = params.getOrDefault("vnp_IpAddr", "unknown");

        log.info("[VNPAY][RETURN] Handling return flow for txnRef={} IP={}", txnRef, ipAddress);

        try {
            // [NGUYÊN BẢN] Verify signature (BLACKBOX)
            boolean isValid = VnpaySignatureUtil.verifySignature(params, vnpayConfig.getHashSecret().trim());
            if (!isValid) {
                log.error("[VNPAY][RETURN] Signature invalid! txnRef={}", txnRef);
                return; // Fallback redirect to frontend error page later in controller
            }
            log.info("[VNPAY][RETURN] Signature valid!");

            // [MỚI] Simulate IPN bằng cách gọi chung hàm processPayment để update DB nếu IPN chưa kịp đến
            boolean result = processPayment(params);
            log.info("[VNPAY][RETURN] Simulate IPN result status for txnRef={}: {}", txnRef, result);

        } catch (Exception e) {
            // [MỚI] Nuốt chặt Exception để KHÔNG văng lỗi 500, controller vẫn giữ quyền redirect browser
            log.error("[VNPAY][RETURN] Unexpected error during simulated IPN on return, txnRef={}", txnRef, e);
        }
    }

    @Override
    @Transactional
    public boolean handleVNPayIpn(HttpServletRequest request) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("ipn_debug.log", true))) {
            writer.println("================ IPN WEBHOOK RECEIVED ================");
            writer.println("Time: " + LocalDateTime.now());
            log.info("================ IPN WEBHOOK RECEIVED ================");
            writer.println("[VNPAY DEBUG] Raw QueryString: " + request.getQueryString());
            
            // [NGUYÊN BẢN] Lấy Raw params
            Map<String, String> params = VnpaySignatureUtil.extractRawParams(request);

            // [NGUYÊN BẢN] Verify Signature (BLACKBOX)
            boolean isValid = VnpaySignatureUtil.verifySignature(params, vnpayConfig.getHashSecret().trim());
            if (!isValid) {
                log.error("[VNPAY DEBUG] FAILED: Signature mismatch.");
                writer.println("[VNPAY DEBUG] FAILED: Signature mismatch.");
                return false;
            }

            log.info("[VNPAY DEBUG] SUCCESS: Signature is absolutely valid.");
            
            // [MỚI] Delegate toàn bộ business logic cho method dùng chung
            boolean processSuccess = processPayment(params);
            
            writer.println("================ IPN WEBHOOK COMPLETE ================");
            log.info("================ IPN WEBHOOK COMPLETE ================");
            
            return processSuccess;

        } catch (Exception e) {
            log.error("[VNPAY DEBUG] FAILED: Unexpected fatal IPN error: ", e);
            return false;
        }
    }

    /**
     * [MỚI] Method cốt lõi xử lý update Transaction và kích hoạt Subscription.
     * Chạy an toàn kể cả khi gọi n lần.
     */
    private boolean processPayment(Map<String, String> params) {
        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String vnpAmountStr = params.get("vnp_Amount");

        log.info("[CORE PAYMENT PROCESS] Verifying logic for txnRef={}", txnRef);

        if (txnRef == null || responseCode == null || vnpAmountStr == null) {
            log.error("[CORE PAYMENT PROCESS] Missing explicit required params txnRef or responseCode.");
            return false;
        }

        // 1. Validate transaction existence
        var optionalTx = paymentRepo.findByTransactionCode(txnRef);
        if (optionalTx.isEmpty()) {
            log.error("[CORE PAYMENT PROCESS] FAILED: TRANSACTION NOT FOUND txnRef={}", txnRef);
            return false;
        }
        PaymentTransaction tx = optionalTx.get();

        // 2. Validate amount
        long vnpAmount;
        try {
            vnpAmount = Long.parseLong(vnpAmountStr);
        } catch (NumberFormatException e) {
            log.error("[CORE PAYMENT PROCESS] Amount parse error! raw={}", vnpAmountStr);
            return false;
        }

        long expectedAmount = tx.getAmount() * 100L;
        if (vnpAmount != expectedAmount) {
            log.error("[CORE PAYMENT PROCESS] Amount mismatch! txnRef={}, DB={}, VNPay={}", txnRef, expectedAmount, vnpAmount);
            return false;
        }

        // 3. Validate idempotency (đã thanh toán chưa)
        if (tx.isSuccess()) {
            log.info("[CORE PAYMENT PROCESS] SUCCESS (ALREADY PROCESSED): Transaction already SUCCESS in DB. Skipping duplicated events.");
            return true;
        }

        // 4. Validate ResponseCode & Update DB Atomically
        if ("00".equals(responseCode)) {
            log.info("[CORE PAYMENT PROCESS] Output response == 00, processing SUCCESS in DB.");
            
            int rows = paymentRepo.updateStatusAtomically(txnRef, PaymentStatus.PENDING, PaymentStatus.SUCCESS, LocalDateTime.now());
            if (rows == 0) {
                log.info("[CORE PAYMENT PROCESS] Duplicate concurrent processing ignored (no rows updated).");
                return true;
            }

            // Kích hoạt Subscription
            log.info("🔥 [CORE PAYMENT PROCESS] SUBSCRIPTION CREATE BEGIN");
            PaymentTransaction fresh = paymentRepo.findByTransactionCode(txnRef).orElse(tx);
            adminSubscriptionRequestService.createFromPaymentSuccess(fresh);
            log.info("🔥 [CORE PAYMENT PROCESS] SUBSCRIPTION CREATE FINISHED");
            
        } else {
            log.warn("[CORE PAYMENT PROCESS] FAILED: Payment unsuccessful. ResponseCode={}.", responseCode);
            paymentRepo.updateStatusAtomically(txnRef, PaymentStatus.PENDING, PaymentStatus.FAILED, null);
        }

        return true;
    }
}
