package com.example.SmartCV.modules.subscription.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.subscription.domain.SubscriptionUsage;
import com.example.SmartCV.modules.subscription.domain.UsageType;

public interface SubscriptionUsageRepository extends JpaRepository<SubscriptionUsage, Long> {

    boolean existsByUserIdAndCvIdAndUsageType(Long userId, Long cvId, UsageType usageType);

    long countByUserIdAndUsageTypeAndPeriod(Long userId, UsageType usageType, String period);

    Optional<SubscriptionUsage> findByUserIdAndCvIdAndUsageType(Long userId, Long cvId, UsageType usageType);

    List<SubscriptionUsage> findByExpireAtBefore(LocalDateTime time);

    List<SubscriptionUsage> findByExpireAtBeforeAndNotifiedBeforeExpireFalse(LocalDateTime time);
}
