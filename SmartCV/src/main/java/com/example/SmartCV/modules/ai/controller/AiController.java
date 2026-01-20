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

    @PostMapping("/cv/chat")
    public ResponseEntity<AiChatResponse> chatWithCv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AiChatRequest request) {
        Long userId = principal.getId();

        // Check daily usage limit (NOT plan-based)
        aiUsageService.checkAndRecordUsage(userId);

        CV cv = cvService.getMyCVDetail(userId, request.getCvId());

        String result = aiService.chatWithCv(cv.getContent(), request.getMessage());

        return ResponseEntity.ok(new AiChatResponse(result));
    }
}
