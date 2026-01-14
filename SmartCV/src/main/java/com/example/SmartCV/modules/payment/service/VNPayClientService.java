package com.example.SmartCV.modules.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.payment.domain.PaymentTransaction;

@Service
public class VNPayClientService {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.pay-url}")
    private String payUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

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
        params.put("vnp_IpAddr", "127.0.0.1");

        params.put(
            "vnp_CreateDate",
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(tx.getCreatedAt())
        );

        String query = buildQuery(params);
        String secureHash = hmacSHA512(hashSecret, query);

        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    /* ================= HELPERS ================= */

    private String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8));
            sb.append("=");
            sb.append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            sb.append("&");
        }
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
            throw new RuntimeException("Cannot generate VNPay hash", e);
        }
    }
}
