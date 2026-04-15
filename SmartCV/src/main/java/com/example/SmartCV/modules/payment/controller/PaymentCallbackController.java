package com.example.SmartCV.modules.payment.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.payment.service.PaymentCallbackService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final PaymentCallbackService paymentCallbackService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * =========================
     * VNPay RETURN URL
     * - User browser redirect về
     * - Chỉ để hiển thị kết quả
     * - KHÔNG dùng để xác nhận giao dịch (DO NOT update DB here)
     * =========================
     */
    @GetMapping("/return")
    public ResponseEntity<Void> vnpayReturn(HttpServletRequest request) {

        try {
            // Verify signature only, do NOT update DB. Only log what happens.
            paymentCallbackService.handleVNPayReturn(request);
        } catch (Exception e) {
            log.error("[VNPAY][RETURN] Error rendering return url", e);
            // Log error but still redirect to failure page
        }

        // Extract display values from query string for redirect to frontend
        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef       = request.getParameter("vnp_TxnRef");

        // Redirect user browser to frontend application result page
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
     * - DÙNG IPN để xác nhận giao dịch (Validate and update DB)
     * =========================
     */
    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(HttpServletRequest request) {

        log.info("[VNPAY][IPN] Received IPN webhook notification from VNPAY");
        
        boolean success = paymentCallbackService.handleVNPayIpn(request);

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
