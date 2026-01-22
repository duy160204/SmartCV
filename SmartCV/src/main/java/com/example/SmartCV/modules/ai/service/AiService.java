package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.ai.client.AiProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;

    public String chatWithCv(String cvContent, String userMessage) {

        String prompt = AiPrompts.SAFETY_INSTRUCTIONS + "\n" + AiPrompts.buildCvReviewPrompt(cvContent, userMessage);

        return aiProvider.chat(prompt);
    }
}
