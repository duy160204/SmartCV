package com.example.SmartCV.modules.ai.service;

import org.springframework.stereotype.Service;

import com.example.SmartCV.modules.ai.client.OpenAiClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiService {

    private final OpenAiClient openAiClient;

    public String chatWithCv(String cvContent, String userMessage) {

        String prompt = buildPrompt(cvContent, userMessage);

        return openAiClient.chat(prompt);
    }

    private String buildPrompt(String cvContent, String userMessage) {
        return """
Bạn là chuyên gia tuyển dụng và hướng nghiệp.
Nhiệm vụ của bạn là đánh giá CV và đưa ra góp ý thực tế, dễ hiểu, chi tiết.

=== NỘI DUNG CV ===
%s

=== CÂU HỎI CỦA NGƯỜI DÙNG ===
%s

Yêu cầu:
- Nhận xét thẳng thắn
- Chỉ ra điểm mạnh, điểm yếu
- Gợi ý cải thiện cụ thể
- Nếu thiếu thông tin thì nói rõ là thiếu gì
""".formatted(cvContent, userMessage);
    }
}
