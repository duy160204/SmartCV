package com.example.SmartCV.modules.payment.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * ============================================================
 * VNPay Signature Utility — CANONICAL VERSION (2.1.0)
 * ============================================================
 *
 * CANONICAL RULE (ONE RULE FOR BOTH CREATE AND VERIFY):
 *
 *   hash_input = sort(params by key ASCII)
 *              .filter(key starts with "vnp_")
 *              .exclude(vnp_SecureHash, vnp_SecureHashType)
 *              .map(k, v -> k + "=" + URLEncode(v, UTF-8))
 *              .join("&")
 *
 * CREATE: params hold PLAIN text values → URLEncode each for hash
 * VERIFY: params hold URL-encoded values (from VNPay query string) → they
 *         ARE already encoded → use as-is in hash (byte-for-byte identical)
 *
 * EXCLUDED FROM HASH AND PAYLOAD:
 *   - vnp_SecureHash
 *   - vnp_SecureHashType
 *   - vnp_IpnUrl (configured at merchant portal, NOT sent as param)
 */
@Slf4j
public final class VnpaySignatureUtil {

    private VnpaySignatureUtil() {}

    private static final Set<String> HASH_EXCLUDE = Set.of(
            "vnp_SecureHash",
            "vnp_SecureHashType"
    );

    // ----------------------------------------------------------------
    // 1. BUILD CANONICAL HASH INPUT  (same rule for CREATE and VERIFY)
    // ----------------------------------------------------------------

    /**
     * Build hash-input string from a map of PLAIN (non-encoded) values.
     *
     * Rule: key=URLEncode(value)&key=URLEncode(value)
     *
     * Called by: buildPaymentUrl (CREATE)
     */
    public static String buildHashData(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : params.entrySet()) {
            String key   = e.getKey();
            String value = e.getValue();
            if (key == null || !key.startsWith("vnp_")) continue;
            if (HASH_EXCLUDE.contains(key))               continue;
            if (value == null || value.isBlank())         continue;
            sorted.put(key, value);
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (!first) sb.append('&');
            sb.append(e.getKey())
              .append('=')
              .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
            first = false;
        }

        log.info("[VNPAY][canonical_string_create] {}", sb);
        return sb.toString();
    }

    // ----------------------------------------------------------------
    // 2. BUILD URL QUERY STRING  (URL-encoded, for browser redirect)
    // ----------------------------------------------------------------

    /**
     * Build a URL-encoded query string from PLAIN params.
     * Called AFTER hash is computed; vnp_SecureHash appended separately.
     */
    public static String buildQueryString(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            String value = e.getValue();
            if (value == null || value.isBlank()) continue;
            if (!first) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), StandardCharsets.US_ASCII))
              .append('=')
              .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
            first = false;
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------
    // 3. EXTRACT RAW PARAMS FROM INCOMING REQUEST
    // ----------------------------------------------------------------

    /**
     * Extract params verbatim from raw HTTP query string.
     * Values are kept URL-encoded as sent by VNPay.
     */
    public static Map<String, String> extractRawParams(HttpServletRequest request) {
        TreeMap<String, String> params = new TreeMap<>();
        String rawQuery = request.getQueryString();
        if (rawQuery == null || rawQuery.isEmpty()) return params;

        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx == -1) continue;
            String key   = pair.substring(0, idx);
            String value = pair.substring(idx + 1);
            if (!key.isEmpty()) params.put(key, value);
        }
        return params;
    }

    // ----------------------------------------------------------------
    // 4. HMAC SHA512
    // ----------------------------------------------------------------

    public static String hmacSHA512(String key, String data) {
        try {
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA512");
            hmac.init(new SecretKeySpec(keyBytes, "HmacSHA512"));
            byte[] result = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder(128);
            for (byte b : result) sb.append(String.format("%02X", b & 0xff));
            return sb.toString().toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException("HMAC SHA512 failed", e);
        }
    }

    // ----------------------------------------------------------------
    // 5. VERIFY SIGNATURE  (IPN / RETURN)
    // ----------------------------------------------------------------

    /**
     * Verify incoming VNPay signature.
     *
     * CANONICAL RULE applied here identically to buildHashData():
     *   rawParams values ARE already URL-encoded (from VNPay query string).
     *   VNPay signed using the same URL-encoded format.
     *   → Use them as-is in hash input (byte-for-byte identical to CREATE).
     *
     * Symmetry guarantee:
     *   CREATE hash input  = url_encode(plain_value)
     *   VERIFY hash input  = url_encoded_value_from_vnpay   (same bytes)
     */
    public static boolean verifySignature(Map<String, String> rawParams, String secretKey) {
        String receivedHash = rawParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isEmpty()) {
            log.error("[VNPAY] Missing vnp_SecureHash in incoming request");
            return false;
        }

        // Build canonical string from already-URL-encoded values (from VNPay query string)
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> e : rawParams.entrySet()) {
            String key   = e.getKey();
            String value = e.getValue();
            if (key == null || !key.startsWith("vnp_")) continue;
            if (HASH_EXCLUDE.contains(key))               continue;
            if (value == null || value.isBlank())         continue;
            sorted.put(key, value);
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            if (!first) sb.append('&');
            // Values are already URL-encoded as VNPay sent them — use as-is
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }

        String hashData       = sb.toString();
        String calculatedHash = hmacSHA512(secretKey, hashData);

        log.info("========== SIGNATURE VERIFICATION ==========");
        log.info("[VNPAY][canonical_string_verify]  {}", hashData);
        log.info("[VNPAY][final_secure_hash_create] {}", calculatedHash);
        log.info("[VNPAY][received_secure_hash]     {}", receivedHash);
        log.info("============================================");

        boolean match = calculatedHash.equalsIgnoreCase(receivedHash);
        if (!match) log.error("[VNPAY] SIGNATURE MISMATCH!");
        return match;
    }
}
