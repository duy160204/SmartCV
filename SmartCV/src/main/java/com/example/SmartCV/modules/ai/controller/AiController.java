package com.example.SmartCV.modules.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.SmartCV.common.utils.UserPrincipal;
import com.example.SmartCV.modules.ai.dto.AiChatRequest;
import com.example.SmartCV.modules.ai.dto.AiChatResponse;
import com.example.SmartCV.modules.ai.service.AiGateway;
import com.example.SmartCV.modules.cv.domain.CV;
import com.example.SmartCV.modules.cv.service.CVService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiGateway aiGateway;
    private final CVService cvService;

    @PostMapping({ "/cv/chat", "/cv/chat/" })
    public ResponseEntity<AiChatResponse> chatWithCv(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody AiChatRequest request) {
        Long userId = principal.getId();

        CV cv = cvService.getMyCVDetail(userId, request.getCvId());

        // GATEWAY is now the only entry point for quota + execution
        String result = aiGateway.chatWithCv(userId, cv.getContent(), request.getMessage(), request.getLevel(), request.getLocale());

        return ResponseEntity.ok(new AiChatResponse(result));
    }

    @PostMapping({ "/cv/generate", "/cv/generate/" })
    public ResponseEntity<AiChatResponse> generateCvContent(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody com.example.SmartCV.modules.ai.dto.AiGenerateRequest request) {
        
        String result = aiGateway.generateCvContent(principal.getId(), request.getPrompt(), request.getTemplateConfigJson(), request.getLocale());
        return ResponseEntity.ok(new AiChatResponse(result));
    }

    @PostMapping({ "/text/improve", "/text/improve/" })
    public ResponseEntity<AiChatResponse> improveText(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody com.example.SmartCV.modules.ai.dto.AiImproveRequest request) {
        
        String result = aiGateway.improveText(principal.getId(), request.getText(), request.getInstruction(), request.getLocale());
        return ResponseEntity.ok(new AiChatResponse(result));
    }

    @PostMapping({ "/template/build", "/template/build/" })
    public ResponseEntity<AiChatResponse> buildTemplate(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody com.example.SmartCV.modules.ai.dto.AiBuildTemplateRequest request) {
        
        String result = aiGateway.buildTemplateFromImage(principal.getId(), request.getImageUrl(), request.getLocale());
        return ResponseEntity.ok(new AiChatResponse(result));
    }
}
