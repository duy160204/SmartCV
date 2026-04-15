package com.example.SmartCV.modules.payment.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * ============================================================
 * VNPay Signature Utility — PRODUCTION SECURE VERSION (2.1.0)
 * ============================================================
 */
@Slf4j
public final class VnpaySignatureUtil {

    private VnpaySignatureUtil() {}

    /**
     * 1. Extract RAW params from request string
     * Keeps the exact encoded values format sent by VNPay
     */
    public static Map<String, String> extractRawParams(HttpServletRequest request) {
        TreeMap<String, String> params = new TreeMap<>();
        String rawQueryString = request.getQueryString();
        
        if (rawQueryString == null || rawQueryString.isEmpty()) {
            return params;
        }

        String[] pairs = rawQueryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) continue;

            String key = pair.substring(0, idx);
            String value = pair.substring(idx + 1); // Keep RAW encoded value

            if (!key.isEmpty()) {
                params.put(key, value);
            }
        }
        return params;
    }

    /**
     * 2. Build hash string for CREATE PAYMENT
     * Encodes raw map keys & values into URL-encoded format
     */
    public static String buildHashData(Map<String, String> params) {
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.isEmpty()) continue;
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) continue;

            if (!first) sb.append('&');
            sb.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
              .append('=')
              .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }

    /**
     * 3. HMAC SHA512 using UPPERCASE output
     */
    public static String hmacSHA512(String key, String data) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(keyBytes, "HmacSHA512"));
            byte[] result = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(128);
            for (byte b : result) {
                sb.append(String.format("%02X", b & 0xff));
            }
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA512", e);
        }
    }

    /**
     * 4. Verify signature of incoming request
     * Rebuilds hash string directly from RAW IPN/Return parameters
     */
    public static boolean verifySignature(Map<String, String> rawParams, String secretKey) {
        String vnpSecureHash = rawParams.get("vnp_SecureHash");
        
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            log.error("[VNPAY] Missing vnp_SecureHash");
            return false;
        }

        TreeMap<String, String> sortedParams = new TreeMap<>(rawParams);
        sortedParams.remove("vnp_SecureHash");
        sortedParams.remove("vnp_SecureHashType");

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!first) sb.append('&');
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }

        String hashData = sb.toString();
        String calculatedHash = hmacSHA512(secretKey, hashData);

        log.info("========== SIGNATURE VERIFICATION ==========");
        log.info("[VNPAY] Raw HashData:    {}", hashData);
        log.info("[VNPAY] Calculated Hash: {}", calculatedHash);
        log.info("[VNPAY] Received Hash:   {}", vnpSecureHash);
        log.info("============================================");

        boolean match = calculatedHash.equalsIgnoreCase(vnpSecureHash);
        if (!match) log.error("SIGNATURE MISMATCH!");
        return match;
    }
}
