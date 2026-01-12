package com.example.SmartCV.modules.admin.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.admin.dto.AdminUserDetailResponse;
import com.example.SmartCV.modules.admin.dto.AdminUserListResponse;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.EmailService;
import com.example.SmartCV.modules.subscription.domain.UserSubscription;
import com.example.SmartCV.modules.subscription.repository.UserSubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserService.class);

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final EmailService emailService;

    // =========================
    // LIST USERS
    // =========================
    @Transactional(readOnly = true)
    public List<AdminUserListResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    // =========================
    // USER DETAIL
    // =========================
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {

        User user = getUserOrThrow(userId);
        UserSubscription sub = userSubscriptionRepository.findByUserId(userId).orElse(null);

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .verified(user.isVerified())
                .locked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .plan(sub != null ? sub.getPlan() : null)
                .subscriptionStatus(sub != null ? sub.getStatus() : null)
                .startDate(sub != null ? sub.getStartDate() : null)
                .endDate(sub != null ? sub.getEndDate() : null)
                .build();
    }

    // =========================
    // LOCK USER
    // =========================
    public void lockUser(Long userId) {

        User user = getUserOrThrow(userId);

        if (user.isLocked()) {
            throw new RuntimeException("User is already locked");
        }

        user.setLocked(true);
        userRepository.save(user);

        emailService.sendAccountLockedEmail(user.getEmail());

        log.info("[ADMIN][LOCK] userId={} email={}", userId, user.getEmail());
    }

    // =========================
    // UNLOCK USER
    // =========================
    public void unlockUser(Long userId) {

        User user = getUserOrThrow(userId);

        if (!user.isLocked()) {
            throw new RuntimeException("User is not locked");
        }

        user.setLocked(false);
        userRepository.save(user);

        emailService.sendAccountUnlockedEmail(user.getEmail());

        log.info("[ADMIN][UNLOCK] userId={} email={}", userId, user.getEmail());
    }

    // =========================
    // PRIVATE HELPERS
    // =========================
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id = " + userId));
    }

    private AdminUserListResponse toListResponse(User user) {

        UserSubscription sub = userSubscriptionRepository.findByUserId(user.getId()).orElse(null);

        return AdminUserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .verified(user.isVerified())
                .locked(user.isLocked())
                .plan(sub != null ? sub.getPlan() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
