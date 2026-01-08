package com.example.SmartCV.modules.subscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.subscription.domain.UserSubscription;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    Optional<UserSubscription> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
