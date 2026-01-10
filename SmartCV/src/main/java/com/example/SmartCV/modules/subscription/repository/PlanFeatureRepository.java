package com.example.SmartCV.modules.subscription.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.subscription.domain.PlanFeature;
import com.example.SmartCV.modules.subscription.domain.PlanType;

public interface PlanFeatureRepository extends JpaRepository<PlanFeature, Long> {

    List<PlanFeature> findByPlan(PlanType plan);

    Optional<PlanFeature> findByPlanAndFeatureCode(PlanType plan, String featureCode);
}
