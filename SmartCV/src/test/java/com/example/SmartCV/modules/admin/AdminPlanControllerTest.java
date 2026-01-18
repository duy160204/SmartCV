package com.example.SmartCV.modules.admin;

import com.example.SmartCV.modules.admin.controller.AdminPlanController;
import com.example.SmartCV.modules.subscription.domain.PlanType;
import com.example.SmartCV.modules.subscription.dto.CreatePlanRequest;
import com.example.SmartCV.modules.subscription.dto.PlanDefinitionDTO;
import com.example.SmartCV.modules.subscription.service.PlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPlanController.class)
public class AdminPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlanService planService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createPlan_Success() throws Exception {
        CreatePlanRequest req = new CreatePlanRequest();
        req.setCode("TEST_PLAN");
        req.setName("Test Plan");
        req.setPrice(new BigDecimal("100000"));
        req.setDurationMonths(1);
        req.setPlanType(PlanType.PRO);
        req.setMaxSharePerMonth(10);
        req.setPublicLinkExpireDays(30);
        req.setDescription("Test Description");

        PlanDefinitionDTO response = PlanDefinitionDTO.builder()
                .code("TEST_PLAN")
                .name("Test Plan")
                .price(new BigDecimal("100000"))
                .durationMonths(1)
                .planType(PlanType.PRO)
                .build();

        when(planService.createPlan(any(CreatePlanRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/admin/plans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createPlan_Conflict() throws Exception {
        CreatePlanRequest req = new CreatePlanRequest();
        req.setCode("EXISTING_PLAN");
        req.setName("Existing Plan");
        req.setPrice(new BigDecimal("100000"));
        req.setDurationMonths(1);
        req.setPlanType(PlanType.PRO);
        req.setMaxSharePerMonth(10);
        req.setPublicLinkExpireDays(30);

        when(planService.createPlan(any(CreatePlanRequest.class)))
                .thenThrow(new com.example.SmartCV.common.exception.BusinessException("Plan code already exists",
                        org.springframework.http.HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/admin/plans")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }
}
