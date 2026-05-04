package com.example.SmartCV.modules.ai.client;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiProviderFactory {

    private final AiProvider openAiProvider;

    public AiProviderFactory(List<AiProvider> providerList) {
        this.openAiProvider = providerList.stream()
            .filter(p -> p.getType() == AiProviderType.OPENAI)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("OpenAI Provider not found"));
    }

    public AiProvider getPrimaryProvider() {
        return openAiProvider;
    }
    
    // Legacy support for refactoring phase
    public AiProvider getFallbackProvider() {
        return openAiProvider;
    }

    public AiProvider getProvider() {
        return openAiProvider;
    }
}
