package com.example.SmartCV.modules.payment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.payment.service.PaymentCallbackService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class PaymentCallbackController {

    private final PaymentCallbackService paymentCallbackService;

    /**
     * =========================
     * VNPay RETURN URL
     * - User browser redirect về
     * - Chỉ để hiển thị kết quả
     * - KHÔNG dùng để xác nhận giao dịch
     * =========================
     *
     * Ví dụ:
     * /api/payments/vnpay/return?vnp_ResponseCode=00&vnp_TxnRef=xxx
     */
    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * =========================
     * VNPay RETURN URL
     * - User browser redirect về
     * - Chỉ để hiển thị kết quả
     * - KHÔNG dùng để xác nhận giao dịch
     * =========================
     *
     * Ví dụ:
     * /api/payments/vnpay/return?vnp_ResponseCode=00&vnp_TxnRef=xxx
     */
    @GetMapping("/return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {

        try {
            // Verify signature only, do NOT update DB
            paymentCallbackService.handleVNPayReturn(params);
        } catch (Exception e) {
            // Log error but still redirect to failure page
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");

        String redirectUrl = String.format("%s/payment/return?vnp_ResponseCode=%s&vnp_TxnRef=%s",
                frontendUrl, responseCode, txnRef);

        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }

    /**
     * =========================
     * VNPay IPN (Instant Payment Notification)
     * - Server VNPay gọi -> Server mình
     * - DÙNG IPN để xác nhận giao dịch
     * =========================
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(
            @RequestParam Map<String, String> params) {
        boolean success = paymentCallbackService.handleVNPayIpn(params);

        if (success) {
            return ResponseEntity.ok(Map.of(
                    "RspCode", "00",
                    "Message", "Confirm Success"));
        }

        return ResponseEntity.ok(Map.of(
                "RspCode", "99",
                "Message", "Confirm Failed"));
    }
}
