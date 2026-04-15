package com.example.SmartCV.modules.payment.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VNPayHashTest2 {
    public static void main(String[] args) throws Exception {
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
        StringBuilder hashDataEncoded = new StringBuilder();
        StringBuilder hashDataRaw = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String k = fieldNames.get(i);
            String v = params.get(k);
            hashDataRaw.append(k).append('=').append(v);
            hashDataEncoded.append(k).append('=').append(URLEncoder.encode(v, "US-ASCII"));
            if (i < fieldNames.size() - 1) {
                hashDataRaw.append('&');
                hashDataEncoded.append('&');
            }
        }
        System.out.println("Raw Hashing: " + hmac("XJKV8BJ78ZUW1WG64RYVVZZQYNZTAUS", hashDataRaw.toString()));
        System.out.println("Enc Hashing: " + hmac("XJKV8BJ78ZUW1WG64RYVVZZQYNZTAUS", hashDataEncoded.toString()));
    }
    static String hmac(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(2 * raw.length);
        for (byte b : raw) hex.append(String.format("%02x", b & 0xff));
        return hex.toString();
    }
}
