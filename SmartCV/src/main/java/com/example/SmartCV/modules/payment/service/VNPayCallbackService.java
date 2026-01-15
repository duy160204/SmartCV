package com.example.SmartCV.modules.payment.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.admin.service.AdminSubscriptionRequestService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackService implements PaymentCallbackService {

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    private final PaymentTransactionRepository paymentRepo;

    // 🔥 SERVICE TẠO REQUEST CHO ADMIN
    private final AdminSubscriptionRequestService adminSubscriptionRequestService;

    /* ===================================================== */
    /* ===================== RETURN URL ==================== */
    /* ===================================================== */

    /**
     * Return URL – chỉ update trạng thái nhẹ (nếu cần)
     * ❌ KHÔNG tạo subscription / request admin
     */
    @Override
    @Transactional
    public void handleVNPayReturn(Map<String, String> params) {
        processCallback(params, false);
    }

    /* ===================================================== */
    /* ======================== IPN ======================== */
    /* ===================================================== */

    /**
     * IPN – nguồn sự thật duy nhất
     * ✅ Chỉ ở đây mới tạo AdminSubscriptionRequest
     */
    @Override
    @Transactional
    public boolean handleVNPayIpn(Map<String, String> params) {
        try {
            processCallback(params, true);
            return true;
        } catch (Exception e) {
            log.error("[VNPAY][IPN] Error", e);
            return false;
        }
    }

    /* ===================================================== */
    /* ===================== CORE LOGIC ==================== */
    /* ===================================================== */

    private void processCallback(Map<String, String> params, boolean isIpn) {

        if (!verifySignature(params)) {
            log.error("[VNPAY] Invalid signature");
            throw new RuntimeException("Invalid VNPay signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        PaymentTransaction tx = paymentRepo
                .findByTransactionCode(txnRef)
                .orElseThrow(() ->
                        new RuntimeException("Transaction not found: " + txnRef));

        // ======================
        // IDPOTENT – CHỈ 1 LẦN
        // ======================
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("[VNPAY] Transaction already SUCCESS: {}", txnRef);
            return;
        }

        // ======================
        // FAILED
        // ======================
        if (!"00".equals(responseCode)) {
            tx.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(tx);

            log.warn("[VNPAY] Payment FAILED: {}", txnRef);
            return;
        }

        // ======================
        // SUCCESS
        // ======================
        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setPaidAt(LocalDateTime.now());
        paymentRepo.save(tx);

        log.info("[VNPAY][SUCCESS] txnRef={}, userId={}", txnRef, tx.getUserId());

        // ==================================================
        // 🔥 CHỈ IPN MỚI ĐƯỢC GỌI AUTO ADMIN FLOW
        // ==================================================
        if (isIpn) {
            adminSubscriptionRequestService
                    .createFromPaymentSuccess(tx);
        }
    }

    /* ===================================================== */
    /* =================== VERIFY SIGNATURE ================= */
    /* ===================================================== */

    private boolean verifySignature(Map<String, String> params) {

        String receivedHash = params.get("vnp_SecureHash");

        Map<String, String> filtered = new TreeMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("vnp_")
                    && !k.equals("vnp_SecureHash")
                    && !k.equals("vnp_SecureHashType")) {
                filtered.put(k, v);
            }
        });

        String data = buildQuery(filtered);
        String expectedHash = hmacSHA512(hashSecret, data);

        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    private String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) ->
                sb.append(k).append("=").append(v).append("&"));
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA512");
            var secretKey =
                    new javax.crypto.spec.SecretKeySpec(
                            key.getBytes(),
                            "HmacSHA512"
                    );
            mac.init(secretKey);

            byte[] raw = mac.doFinal(data.getBytes());
            StringBuilder hex = new StringBuilder(2 * raw.length);
            for (byte b : raw) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Cannot verify VNPay signature", e);
        }
    }
}
