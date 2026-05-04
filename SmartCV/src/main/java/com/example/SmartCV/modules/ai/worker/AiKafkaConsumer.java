package com.example.SmartCV.modules.ai.worker;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.ai.domain.AiAnalysisJob;
import com.example.SmartCV.modules.ai.repository.AiAnalysisJobRepository;
import com.example.SmartCV.modules.ai.service.AiGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class AiKafkaConsumer {

    private final AiGateway aiGateway;
    private final AiAnalysisJobRepository jobRepository;

    @KafkaListener(topics = "ai-request", groupId = "smartcv-ai-group")
    @Transactional
    public void consumeAiRequest(String payload) {
        String jobId = extractFromJson(payload, "jobId");
        
        AiAnalysisJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("AI Job {} not found. Discarding phantom message.", jobId);
            return;
        }
        
        int rowsUpdated = jobRepository.lockJobForProcessing(jobId);
        if (rowsUpdated == 0) {
            log.warn("Atomic Race Condition intercepted! Job {} is already claimed by another worker.", jobId);
            return;
        }

        try {
            String cvContent = extractFromJson(payload, "cvContent");
            String userMessage = extractFromJson(payload, "prompt");

            // ENFORCE GATEWAY POLICY (System userId = 0L)
            String result = aiGateway.chatWithCv(0L, cvContent, userMessage, null, "en");
            
            job.setResult(result);
            job.setStatus(AiAnalysisJob.JobStatus.DONE);
            jobRepository.save(job);
            
            log.info("Finished async AI job {}", jobId);
        } catch (Exception e) {
            log.error("AI execution failed for job {}: {}", jobId, e.getMessage());
            job.setStatus(AiAnalysisJob.JobStatus.PENDING);
            jobRepository.save(job);
            // No throw here to prevent Kafka retry storm if we are rate limited
        }
    }

    private String extractFromJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search) + search.length();
        int end = json.indexOf("\"", start);
        if (start < search.length() || end == -1) return "1"; 
        return json.substring(start, end);
    }
}
