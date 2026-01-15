package com.example.SmartCV.modules.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequest;
import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequestStatus;
import com.example.SmartCV.modules.admin.repository.AdminSubscriptionRequestRepository;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;
import com.example.SmartCV.modules.payment.repository.PaymentTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminSubscriptionRequestService {

    private static final Logger log =
            LoggerFactory.getLogger(AdminSubscriptionRequestService.class);

    private final AdminSubscriptionRequestRepository requestRepository;
    private final PaymentTransactionRepository paymentRepository;

    // ==================================================
    // LIST ALL (ADMIN)
    // ==================================================
    @Transactional(readOnly = true)
    public List<AdminSubscriptionRequest> findAll() {
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    // ==================================================
    // LIST BY STATUS (ADMIN)
    // ==================================================
    @Transactional(readOnly = true)
    public List<AdminSubscriptionRequest> findByStatus(
            AdminSubscriptionRequestStatus status
    ) {
        return requestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    // ==================================================
    // AUTO CREATE – gọi từ PaymentCallbackService
    // ==================================================
    public void createFromPaymentSuccess(PaymentTransaction payment) {

        Long paymentId = payment.getId();

        // ===== Idempotent =====
        if (requestRepository.existsByPaymentId(paymentId)) {
            log.warn(
                "[ADMIN_SUB_REQUEST] already exists for paymentId={}",
                paymentId
            );
            return;
        }

        AdminSubscriptionRequest request =
                AdminSubscriptionRequest.builder()
                        .userId(payment.getUserId())
                        .requestedPlan(payment.getPlan())
                        .months(payment.getMonths())
                        .paymentId(paymentId)
                        .status(AdminSubscriptionRequestStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build();

        requestRepository.save(request);

        log.info(
            "[ADMIN_SUB_REQUEST][CREATE] userId={} plan={} months={} paymentId={}",
            payment.getUserId(),
            payment.getPlan(),
            payment.getMonths(),
            paymentId
        );
    }

    // ==================================================
    // ADMIN PREVIEW
    // ==================================================
    public AdminSubscriptionRequest markPreviewed(
            Long requestId,
            Long adminId
    ) {
        AdminSubscriptionRequest request = getOrThrow(requestId);

        if (request.getStatus() != AdminSubscriptionRequestStatus.PENDING) {
            throw new RuntimeException(
                "Only PENDING request can be previewed"
            );
        }

        request.setStatus(AdminSubscriptionRequestStatus.PREVIEWED);
        request.setPreviewedByAdminId(adminId);
        request.setPreviewedAt(LocalDateTime.now());

        log.info(
            "[ADMIN_SUB_REQUEST][PREVIEW] requestId={} adminId={}",
            requestId,
            adminId
        );

        return requestRepository.save(request);
    }

    // ==================================================
    // ADMIN CONFIRM (SAFE)
    // ==================================================
    public AdminSubscriptionRequest markConfirmed(
            Long requestId,
            Long adminId
    ) {
        AdminSubscriptionRequest request = getOrThrow(requestId);

        // ❌ confirm nhiều lần
        if (request.getStatus() == AdminSubscriptionRequestStatus.CONFIRMED) {
            throw new RuntimeException(
                "Subscription request already CONFIRMED"
            );
        }

        // ❌ sai thứ tự
        if (request.getStatus() != AdminSubscriptionRequestStatus.PREVIEWED) {
            throw new RuntimeException(
                "Request must be PREVIEWED before confirm"
            );
        }

        // 🔥 CHECK PAYMENT
        PaymentTransaction payment =
                paymentRepository.findById(request.getPaymentId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                    "Payment not found: " + request.getPaymentId()
                                )
                        );

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException(
                "Cannot confirm subscription: payment is not SUCCESS"
            );
        }

        // ✅ CONFIRM
        request.setStatus(AdminSubscriptionRequestStatus.CONFIRMED);
        request.setConfirmedByAdminId(adminId);
        request.setConfirmedAt(LocalDateTime.now());

        log.info(
            "[ADMIN_SUB_REQUEST][CONFIRMED] requestId={} adminId={} paymentId={}",
            requestId,
            adminId,
            payment.getId()
        );

        return requestRepository.save(request);
    }

    // ==================================================
    // HELPER
    // ==================================================
    private AdminSubscriptionRequest getOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                            "AdminSubscriptionRequest not found: " + id
                        ));
    }
}
