package com.example.SmartCV.modules.subscription.service;

import com.example.SmartCV.modules.subscription.domain.PlanDefinition;
import com.example.SmartCV.modules.subscription.dto.CreatePlanRequest;
import com.example.SmartCV.modules.subscription.dto.PlanDefinitionDTO;
import com.example.SmartCV.modules.subscription.dto.UpdatePlanRequest;
import com.example.SmartCV.modules.subscription.repository.PlanDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanService {

    private final PlanDefinitionRepository planRepository;

    public List<PlanDefinitionDTO> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlanDefinitionDTO> getActivePlans() {
        return planRepository.findAll().stream()
                .filter(PlanDefinition::isActive)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PlanDefinitionDTO createPlan(CreatePlanRequest req) {
        if (planRepository.findByCode(req.getCode()).isPresent()) {
            throw new com.example.SmartCV.common.exception.BusinessException(
                    "Plan code already exists: " + req.getCode(), org.springframework.http.HttpStatus.CONFLICT);
        }

        PlanDefinition plan = PlanDefinition.builder()
                .code(req.getCode())
                .name(req.getName())
                .price(req.getPrice())
                .durationMonths(req.getDurationMonths())
                .plan(req.getPlanType())
                .maxSharePerMonth(req.getMaxSharePerMonth())
                .publicLinkExpireDays(req.getPublicLinkExpireDays())
                .description(req.getDescription())
                .isActive(true)
                .currency("VND")
                .build();

        return toDTO(planRepository.save(plan));
    }

    public PlanDefinitionDTO updatePlan(Long id, UpdatePlanRequest req) {
        PlanDefinition plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        if (req.getName() != null)
            plan.setName(req.getName());
        if (req.getPrice() != null)
            plan.setPrice(req.getPrice());
        if (req.getDurationMonths() > 0)
            plan.setDurationMonths(req.getDurationMonths());
        if (req.getMaxSharePerMonth() > 0)
            plan.setMaxSharePerMonth(req.getMaxSharePerMonth());
        if (req.getPublicLinkExpireDays() > 0)
            plan.setPublicLinkExpireDays(req.getPublicLinkExpireDays());
        if (req.getDescription() != null)
            plan.setDescription(req.getDescription());

        return toDTO(planRepository.save(plan));
    }

    public void togglePlanStatus(Long id) {
        PlanDefinition plan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        plan.setActive(!plan.isActive());
        planRepository.save(plan);
    }

    private final com.example.SmartCV.modules.subscription.repository.PlanFeatureRepository featureRepository;

    private PlanDefinitionDTO toDTO(PlanDefinition plan) {

        // 1. Base Limits
        java.util.List<String> features = new java.util.ArrayList<>();

        // Share Limit
        if (plan.getMaxSharePerMonth() == -1) {
            features.add("Unlimited Public Shares");
        } else {
            features.add("Up to " + plan.getMaxSharePerMonth() + " Public Shares/Month");
        }

        // Link Expiry
        features.add("Links valid for " + plan.getPublicLinkExpireDays() + " days");

        // 2. Dynamic Features from DB
        featureRepository.findByPlan(plan.getPlan())
                .forEach(f -> {
                    if (f.isEnabled()) {
                        switch (f.getFeatureCode()) {
                            case "DOWNLOAD_CV" -> features.add("Download CV as PDF");
                            case "CREATE_CV" -> features.add("Create Unlimited CVs");
                            // Add more mapings as needed or use DB name if available
                            default -> features.add(convertToTitleCase(f.getFeatureCode()));
                        }
                    }
                });

        // 3. Static/Common (Example)
        features.add("Professional Templates");

        return PlanDefinitionDTO.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .durationMonths(plan.getDurationMonths())
                .planType(plan.getPlan())
                .maxSharePerMonth(plan.getMaxSharePerMonth())
                .publicLinkExpireDays(plan.getPublicLinkExpireDays())
                .description(plan.getDescription())
                .active(plan.isActive())
                .features(features)
                .build();
    }

    private String convertToTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder converted = new StringBuilder();

        boolean convertNext = true;
        for (char ch : text.replace('_', ' ').toCharArray()) {
            if (Character.isSpaceChar(ch)) {
                convertNext = true;
            } else if (convertNext) {
                ch = Character.toTitleCase(ch);
                convertNext = false;
            } else {
                ch = Character.toLowerCase(ch);
            }
            converted.append(ch);
        }

        return converted.toString();
    }
}
