package com.example.SmartCV.modules.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;
// import com.example.SmartCV.modules.admin.service.AdminSubscriptionRequestService; // REMOVED

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayCallbackService implements PaymentCallbackService {

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.hashSecret = this.hashSecret.trim();
    }

    private final PaymentTransactionRepository paymentRepo;
    // private final AdminSubscriptionRequestService
    // adminSubscriptionRequestService; // REMOVED PHASE 4
    private final com.example.SmartCV.modules.subscription.service.SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void handleVNPayReturn(Map<String, String> params) {
        processCallback(params, false);
    }

    @Override
    @Transactional
    public boolean handleVNPayIpn(Map<String, String> params) {
        try {
            processCallback(params, true);
            return true;
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            log.warn(
                    "[VNPAY][IPN] Concurrent update detected for params: {}. Transaction already processed or processing.",
                    params);
            return true; // Treat as handled (idempotent)
        } catch (Exception e) {
            log.error("[VNPAY][IPN] Error", e);
            return false;
        }
    }

    private void processCallback(Map<String, String> params, boolean isIpn) {

        if (!verifySignature(params)) {
            log.error("[VNPAY] Invalid signature");
            throw new RuntimeException("Invalid VNPay signature");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        // If it's just a Return URL (redirect), we don't update DB.
        // We just verify signature (done above) and return.
        if (!isIpn) {
            log.info("[VNPAY][RETURN] User returned from VNPay. txnRef={}, code={}", txnRef, responseCode);
            return;
        }

        // --- BELOW IS IPN LOGIC ONLY ---

        PaymentTransaction tx = paymentRepo
                .findByTransactionCode(txnRef)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + txnRef));

        if (tx.getStatus() == PaymentStatus.SUCCESS) {
            log.info("[VNPAY] Transaction already SUCCESS: {}", txnRef);
            return;
        }

        if (!"00".equals(responseCode)) {
            tx.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(tx);
            log.warn("[VNPAY] Payment FAILED: {}", txnRef);
            return;
        }

        tx.setStatus(PaymentStatus.SUCCESS);
        tx.setPaidAt(LocalDateTime.now());
        paymentRepo.save(tx);

        log.info("[VNPAY][IPN][SUCCESS] txnRef={}, userId={}", txnRef, tx.getUserId());

        // AUTO ACTIVE SUBSCRIPTION
        subscriptionService.activateSubscription(tx);
    }

    private boolean verifySignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        Map<String, String> filtered = new TreeMap<>();
        params.forEach((k, v) -> {
            if (k.startsWith("vnp_") && !k.equals("vnp_SecureHash") && !k.equals("vnp_SecureHashType")) {
                filtered.put(k, v);
            }
        });
        String data = buildQuery(filtered);
        String expectedHash = hmacSHA512(hashSecret, data);
        return expectedHash.equalsIgnoreCase(receivedHash);
    }

    private String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                try {
                    sb.append(URLEncoder.encode(k, StandardCharsets.UTF_8.toString()));
                    sb.append("=");
                    sb.append(URLEncoder.encode(v, StandardCharsets.UTF_8.toString()));
                    sb.append("&");
                } catch (Exception e) {
                    log.error("Encoding error", e);
                }
            }
        });
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA512");
            var secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(), "HmacSHA512");
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
