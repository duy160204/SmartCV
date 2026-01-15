package com.example.SmartCV.modules.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartCV.modules.payment.domain.PaymentProvider;
import com.example.SmartCV.modules.payment.domain.PaymentStatus;
import com.example.SmartCV.modules.payment.domain.PaymentTransaction;

@Repository
public interface PaymentTransactionRepository
        extends JpaRepository<PaymentTransaction, Long> {

    // ==================================================
    // CALLBACK (VNPay / Stripe)
    // ==================================================
    Optional<PaymentTransaction>
        findByTransactionCode(String transactionCode);

    // ==================================================
    // USER PAYMENT HISTORY
    // ==================================================
    List<PaymentTransaction>
        findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<PaymentTransaction>
        findTopByUserIdOrderByCreatedAtDesc(Long userId);

    // ==================================================
    // ADMIN – LIST & FILTER
    // ==================================================
    List<PaymentTransaction>
        findAllByOrderByCreatedAtDesc();

    List<PaymentTransaction>
        findByStatusOrderByCreatedAtDesc(
            PaymentStatus status
        );

    List<PaymentTransaction>
        findByProviderOrderByCreatedAtDesc(
            PaymentProvider provider
        );

    // ==================================================
    // ADMIN – DATE RANGE
    // ==================================================
    List<PaymentTransaction>
        findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from,
            LocalDateTime to
        );

    // ==================================================
    // ADMIN – USER + DATE RANGE
    // ==================================================
    List<PaymentTransaction>
        findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime from,
            LocalDateTime to
        );

    // ==================================================
    // DASHBOARD – KPI
    // ==================================================
    long countByStatus(PaymentStatus status);

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM PaymentTransaction p
        WHERE p.status = :status
    """)
    Long sumAmountByStatus(
            @Param("status") PaymentStatus status
    );

    @Query("""
        SELECT COUNT(DISTINCT p.userId)
        FROM PaymentTransaction p
        WHERE p.status = :status
    """)
    long countDistinctUserIdByStatus(
            @Param("status") PaymentStatus status
    );

    // ==================================================
    // DASHBOARD – REVENUE BY DAY (CHART)
    // ==================================================
    @Query("""
        SELECT
            FUNCTION('DATE', p.createdAt),
            SUM(p.amount)
        FROM PaymentTransaction p
        WHERE p.status = 'SUCCESS'
          AND p.createdAt BETWEEN :from AND :to
        GROUP BY FUNCTION('DATE', p.createdAt)
        ORDER BY FUNCTION('DATE', p.createdAt)
    """)
    List<Object[]> revenueByDay(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // ==================================================
    // DASHBOARD – REVENUE BY MONTH (CHART)
    // ==================================================
    @Query("""
        SELECT
            FUNCTION('YEAR', p.createdAt),
            FUNCTION('MONTH', p.createdAt),
            SUM(p.amount)
        FROM PaymentTransaction p
        WHERE p.status = 'SUCCESS'
        GROUP BY
            FUNCTION('YEAR', p.createdAt),
            FUNCTION('MONTH', p.createdAt)
        ORDER BY
            FUNCTION('YEAR', p.createdAt),
            FUNCTION('MONTH', p.createdAt)
    """)
    List<Object[]> revenueByMonth();
}
