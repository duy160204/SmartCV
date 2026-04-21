package com.example.SmartCV.modules.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.ai.dto.AiChatRequest;
import com.example.SmartCV.modules.ai.dto.AiChatResponse;
import com.example.SmartCV.modules.ai.service.AiService;
import com.example.SmartCV.modules.ai.service.AiUsageService;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.service.CVService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiUsageService aiUsageService;
    private final CVService cvService;

    @PostMapping({ "/cv/chat", "/cv/chat/" })
    public ResponseEntity<AiChatResponse> chatWithCv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AiChatRequest request) {
        Long userId = principal.getId();

        // Check daily usage limit (NOT plan-based)
        aiUsageService.checkAndRecordUsage(userId);

        CV cv = cvService.getMyCVDetail(userId, request.getCvId());

        String result = aiService.chatWithCv(cv.getContent(), request.getMessage(), request.getLevel());

        return ResponseEntity.ok(new AiChatResponse(result));
    }

    @PostMapping({ "/cv/generate", "/cv/generate/" })
    public ResponseEntity<AiChatResponse> generateCvContent(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody com.example.SmartCV.modules.ai.dto.AiGenerateRequest request) {
        aiUsageService.checkAndRecordUsage(principal.getId());
        String result = aiService.generateCvContent(request.getPrompt(), request.getTemplateConfigJson());
        return ResponseEntity.ok(new AiChatResponse(result));
    }

    @PostMapping({ "/text/improve", "/text/improve/" })
    public ResponseEntity<AiChatResponse> improveText(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody com.example.SmartCV.modules.ai.dto.AiImproveRequest request) {
        aiUsageService.checkAndRecordUsage(principal.getId());
        String result = aiService.improveText(request.getText(), request.getInstruction());
        return ResponseEntity.ok(new AiChatResponse(result));
    }

    @PostMapping({ "/template/build", "/template/build/" })
    // Note: Assuming Admin tool might be here or in Admin controller. Let's place
    // it here but requiring Admin later if needed.
    public ResponseEntity<AiChatResponse> buildTemplate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody com.example.SmartCV.modules.ai.dto.AiBuildTemplateRequest request) {
        aiUsageService.checkAndRecordUsage(principal.getId());
        String result = aiService.buildTemplateFromImage(request.getImageUrl());
        return ResponseEntity.ok(new AiChatResponse(result));
    }
}
