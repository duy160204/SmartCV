package com.example.SmartCV.modules.subscription.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.subscription.domain.PlanDefinition;
import com.example.SmartCV.modules.subscription.domain.PlanType;

public interface PlanDefinitionRepository extends JpaRepository<PlanDefinition, Long> {

    Optional<PlanDefinition> findByPlan(PlanType plan);

    Optional<PlanDefinition> findByCode(String code);

    Optional<PlanDefinition> findByPlanAndDurationMonths(PlanType plan, int durationMonths);
}
