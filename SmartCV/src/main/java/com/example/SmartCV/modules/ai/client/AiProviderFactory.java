package com.example.SmartCV.modules.ai.client;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiProviderFactory {

    private final Map<AiProviderType, AiProvider> providers = new EnumMap<>(AiProviderType.class);

    @Value("${ai.provider.primary:GEMINI}")
    private AiProviderType primaryType;

    @Value("${ai.provider.fallback:OPENAI}")
    private AiProviderType fallbackType;

    public AiProviderFactory(List<AiProvider> providerList) {
        for (AiProvider provider : providerList) {
            providers.put(provider.getType(), provider);
        }
    }

    public AiProvider getProvider(AiProviderType type) {
        return providers.get(type);
    }

    public AiProvider getPrimaryProvider() {
        return providers.get(primaryType);
    }

    public AiProvider getFallbackProvider() {
        return providers.get(fallbackType);
    }

    public AiProviderType getPrimaryType() {
        return primaryType;
    }
}
