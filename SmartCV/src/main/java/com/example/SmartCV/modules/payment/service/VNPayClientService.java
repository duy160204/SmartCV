package com.example.SmartCV.modules.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.dto.PaymentResponse;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VNPayClientService implements PaymentService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.tmnCode = this.tmnCode != null ? this.tmnCode.trim() : "";
        this.hashSecret = this.hashSecret != null ? this.hashSecret.trim() : "";
    }

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.VNPAY;
    }

    @Override
    public boolean isEnabled() {
        return tmnCode != null && !tmnCode.isBlank();
    }

    @Override
    public PaymentResponse createPayment(PaymentTransaction tx) {
        String paymentUrl = buildPaymentUrl(tx);
        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .provider(getProvider().name())
                .transactionCode(tx.getTransactionCode())
                .build();
    }

    public String buildPaymentUrl(PaymentTransaction tx) {

        Map<String, String> params = new TreeMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(tx.getAmount() * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", tx.getTransactionCode());
        params.put("vnp_OrderInfo", "Thanh toan goi " + tx.getPlan());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", tx.getIpAddress() != null ? tx.getIpAddress() : "127.0.0.1");

        params.put(
                "vnp_CreateDate",
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                        .format(tx.getCreatedAt()));

        // 1. Build Raw Hash Data (No Encoding)
        String hashData = buildHashData(params);

        // 2. Calculate Hash
        String secureHash = hmacSHA512(hashSecret, hashData);

        // 3. Build Encoded URL Query
        String queryUrl = buildQueryUrl(params);
        String finalQuery = queryUrl + "&vnp_SecureHash=" + secureHash;

        log.info("[VNPAY REQUEST] Params: {}", params);
        log.info("[VNPAY REQUEST] Hash Input (RAW): {}", hashData);
        log.info("[VNPAY REQUEST] Generated Hash: {}", secureHash);
        log.info("[VNPAY REQUEST] Final Query: {}", finalQuery);

        return payUrl + "?" + finalQuery;
    }

    /* ================= HELPERS ================= */

    private String buildHashData(Map<String, String> params) {
        // CRITICAL: VNPay official Java demo encodes VALUES with US_ASCII before hashing
        // Ref: sandbox.vnpayment.vn official servlet demo
        // hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> e = itr.next();
            String value = e.getValue();
            if (value != null && !value.isEmpty()) {
                sb.append(e.getKey());
                sb.append('=');
                sb.append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }

    private String buildQueryUrl(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = params.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> e = itr.next();
            String value = e.getValue();
            if (value != null && !value.isEmpty()) {
                sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII));
                sb.append('=');
                sb.append(URLEncoder.encode(value, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    sb.append('&');
                }
            }
        }
        return sb.toString();
    }


    private String hmacSHA512(String key, String data) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA512");
            var secretKey = new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

            mac.init(secretKey);

            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(2 * raw.length);

            for (byte b : raw) {
                hex.append(String.format("%02x", b & 0xff));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Cannot generate VNPay hash", e);
        }
    }
}
