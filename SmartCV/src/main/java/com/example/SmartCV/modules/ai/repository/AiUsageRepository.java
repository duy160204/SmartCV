package com.example.SmartCV.modules.ai.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.ai.domain.AiUsage;

public interface AiUsageRepository extends JpaRepository<AiUsage, Long> {

    Optional<AiUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);
}
