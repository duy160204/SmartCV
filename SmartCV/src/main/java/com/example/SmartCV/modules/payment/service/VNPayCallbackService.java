package com.example.SmartCV.modules.payment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
import com.example.SmartCV.modules.subscription.domain.ChangeReason;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionChangeType;
import com.example.SmartCV.modules.subscription.domain.SubscriptionStatus;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;
import com.example.SmartCV.modules.subscription.service.SubscriptionHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackService implements PaymentCallbackService {

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    private final PaymentTransactionRepository paymentRepo;
    private final UserSubscriptionRepository subscriptionRepo;
    private final SubscriptionHistoryService historyService;

    /* ===================================================== */
    /* ===================== RETURN URL ==================== */
    /* ===================================================== */

    @Override
    @Transactional
    public void handleVNPayReturn(Map<String, String> params) {
        processCallback(params, false);
    }

    /* ===================================================== */
    /* ======================== IPN ======================== */
    /* ===================================================== */

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
            throw new RuntimeException("Invalid signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        PaymentTransaction tx = paymentRepo
                .findByTransactionCode(txnRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        // chống callback trùng
        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("[VNPAY] Transaction already SUCCESS: {}", txnRef);
            return;
        }

        if (!"00".equals(responseCode)) {
            tx.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(tx);
            log.warn("[VNPAY] Payment FAILED: {}", txnRef);
            return;
        }

        // SUCCESS
        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setPaidAt(LocalDateTime.now());
        paymentRepo.save(tx);

        activateSubscription(tx);

        log.info("[VNPAY][SUCCESS] txnRef={}, userId={}", txnRef, tx.getUserId());
    }

    /* ===================================================== */
    /* ================= SUBSCRIPTION LOGIC ================= */
    /* ===================================================== */

    private void activateSubscription(PaymentTransaction tx) {

        Long userId = tx.getUserId();

        UserSubscription sub = subscriptionRepo
                .findByUserId(userId)
                .orElse(null);

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(tx.getMonths());

        PlanType oldPlan = sub != null ? sub.getPlan() : null;

        if (sub == null) {
            sub = UserSubscription.builder()
                    .userId(userId)
                    .plan(tx.getPlan())
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(start)
                    .endDate(end)
                    .build();
        } else {
            sub.setPlan(tx.getPlan());
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setStartDate(start);
            sub.setEndDate(end);
        }

        subscriptionRepo.save(sub);

        // ghi lịch sử
        historyService.saveHistory(
                userId,
                oldPlan,
                tx.getPlan(),
                SubscriptionChangeType.PAYMENT_SUCCESS,
                ChangeReason.PAYMENT,
                tx.getId(),
                null
        );
    }

    /* ===================================================== */
    /* =================== VERIFY SIGNATURE ================= */
    /* ===================================================== */

    private boolean verifySignature(Map<String, String> params) {

        String receivedHash = params.get("vnp_SecureHash");

        Map<String, String> filtered = new TreeMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash")
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
        params.forEach((k, v) -> sb.append(k).append("=").append(v).append("&"));
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA512");
            var secretKey =
                    new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA512");
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
