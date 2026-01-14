package com.example.SmartCV.modules.payment.controller;

import java.util.Map;

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
    @GetMapping("/return")
    public ResponseEntity<?> vnpayReturn(
            @RequestParam Map<String, String> params
    ) {
        paymentCallbackService.handleVNPayReturn(params);

        // Thực tế: nên redirect về frontend
        return ResponseEntity.ok("Payment return processed");
    }

    /**
     * =========================
     * VNPay IPN (Instant Payment Notification)
     * - Server VNPay gọi -> Server mình
     * - DÙNG IPN để xác nhận giao dịch
     * =========================
     */
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> vnpayIpn(
            @RequestParam Map<String, String> params
    ) {
        boolean success = paymentCallbackService.handleVNPayIpn(params);

        if (success) {
            return ResponseEntity.ok(Map.of(
                "RspCode", "00",
                "Message", "Confirm Success"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "RspCode", "99",
            "Message", "Confirm Failed"
        ));
    }
}
