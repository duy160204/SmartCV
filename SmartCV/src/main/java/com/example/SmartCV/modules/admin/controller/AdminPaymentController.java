package com.example.SmartCV.modules.admin.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.modules.admin.service.AdminPaymentService;
import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;

    // ==================================================
    // LIST ALL PAYMENTS
    // ==================================================
    @GetMapping
    public ResponseEntity<List<PaymentTransaction>> listAll() {
        return ResponseEntity.ok(
                adminPaymentService.findAll()
        );
    }

    // ==================================================
    // VIEW PAYMENT DETAIL (🔥 MỚI – XEM RIÊNG 1 PAYMENT)
    // ==================================================
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentTransaction> getDetail(
            @PathVariable Long paymentId
    ) {
        return ResponseEntity.ok(
                adminPaymentService.findById(paymentId)
        );
    }

    // ==================================================
    // FILTER BY USER
    // ==================================================
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentTransaction>> byUser(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                adminPaymentService.findByUserId(userId)
        );
    }

    // ==================================================
    // FILTER BY STATUS
    // ==================================================
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentTransaction>> byStatus(
            @PathVariable PaymentStatus status
    ) {
        return ResponseEntity.ok(
                adminPaymentService.findByStatus(status)
        );
    }

    // ==================================================
    // FILTER BY PROVIDER
    // ==================================================
    @GetMapping("/provider/{provider}")
    public ResponseEntity<List<PaymentTransaction>> byProvider(
            @PathVariable PaymentProvider provider
    ) {
        return ResponseEntity.ok(
                adminPaymentService.findByProvider(provider)
        );
    }

    // ==================================================
    // FILTER BY DATE RANGE
    // ==================================================
    @GetMapping("/date-range")
    public ResponseEntity<List<PaymentTransaction>> byDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return ResponseEntity.ok(
                adminPaymentService.findByDateRange(from, to)
        );
    }

    // ==================================================
    // USER + DATE RANGE
    // ==================================================
    @GetMapping("/search")
    public ResponseEntity<List<PaymentTransaction>> search(
            @RequestParam Long userId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        return ResponseEntity.ok(
                adminPaymentService
                        .findByUserAndDateRange(userId, from, to)
        );
    }
}
