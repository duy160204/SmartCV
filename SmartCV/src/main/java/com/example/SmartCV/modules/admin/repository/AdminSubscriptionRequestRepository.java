package com.example.SmartCV.modules.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequest;
import com.example.SmartCV.modules.admin.domain.AdminSubscriptionRequestStatus;

@Repository
public interface AdminSubscriptionRequestRepository
        extends JpaRepository<AdminSubscriptionRequest, Long> {

    // =========================
    // IDENTITY / SAFETY
    // =========================
    boolean existsByPaymentId(Long paymentId);

    // =========================
    // ADMIN LISTING
    // =========================
    List<AdminSubscriptionRequest>
        findByStatus(AdminSubscriptionRequestStatus status);

    List<AdminSubscriptionRequest>
        findByUserId(Long userId);

    List<AdminSubscriptionRequest>
        findByStatusOrderByCreatedAtDesc(
            AdminSubscriptionRequestStatus status
        );

    List<AdminSubscriptionRequest>
        findAllByOrderByCreatedAtDesc();
}
