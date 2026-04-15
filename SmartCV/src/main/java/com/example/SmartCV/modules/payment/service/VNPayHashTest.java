package com.example.SmartCV.modules.payment.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Standalone VNPay hash tester — chạy main() để verify hash độc lập.
 * KHÔNG dùng Spring context, test thuần Java.
 *
 * Run: javac VNPayHashTest.java && java VNPayHashTest
 */
public class VNPayHashTest {

    private static final String SECRET = "XJKV8BJ78ZUW1WG64RYVVVZ7QYNZTAUS";

    public static void main(String[] args) throws Exception {

        // Params từ log thực tế
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "4335400");
        params.put("vnp_Command", "pay");
        params.put("vnp_CreateDate", "20260414171211");
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_ExpireDate", "20260414172711");
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_Locale", "vn");
        params.put("vnp_OrderInfo", "Thanh toan goi PRO");
        params.put("vnp_OrderType", "other");
        params.put("vnp_ReturnUrl", "http://localhost:8080/api/payments/vnpay/return");
        params.put("vnp_TmnCode", "COGQB1HR");
        params.put("vnp_TxnRef", "ee30ec4a-07e6-4803-837a-32057020d3ae");
        params.put("vnp_Version", "2.1.0");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String k = fieldNames.get(i);
            String v = params.get(k);
            if (v == null || v.isEmpty())
                continue;

            // HASH = RAW (official VNPay spec)
            hashData.append(k).append('=').append(v);
            // QUERY = encoded
            query.append(URLEncoder.encode(k, StandardCharsets.US_ASCII))
                    .append('=')
                    .append(URLEncoder.encode(v, StandardCharsets.UTF_8));

            if (i < fieldNames.size() - 1) {
                hashData.append('&');
                query.append('&');
            }
        }

        System.out.println("=== HASH DATA (RAW) ===");
        System.out.println(hashData);
        System.out.println();
        System.out.println("=== QUERY (ENCODED) ===");
        System.out.println(query);
        System.out.println();

        String hash = hmacSHA512(SECRET, hashData.toString());
        System.out.println("=== SECURE HASH ===");
        System.out.println(hash);
        System.out.println();

        String finalUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?" + query
                + "&vnp_SecureHashType=HmacSHA512"
                + "&vnp_SecureHash=" + hash;
        System.out.println("=== FINAL URL ===");
        System.out.println(finalUrl);
    }

    static String hmacSHA512(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(2 * raw.length);
        for (byte b : raw)
            hex.append(String.format("%02x", b & 0xff));
        return hex.toString();
    }
}
