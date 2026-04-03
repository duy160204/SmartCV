package com.example.SmartCV.modules.ai.worker;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.ai.domain.AiAnalysisJob;
import com.example.SmartCV.modules.ai.repository.AiAnalysisJobRepository;
import com.example.SmartCV.modules.ai.service.AiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpServerErrorException;
import java.util.concurrent.TimeoutException;
import com.example.SmartCV.common.exception.BusinessException;
import org.springframework.context.annotation.Profile;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class AiKafkaConsumer {

    private final AiService aiService;
    private final AiAnalysisJobRepository jobRepository;

    /**
     * DLT Routing & Precise Exceptions:
     * Only retries timeouts, API server crashes (5xx), and rate limits (mapped to BusinessException).
     * Prevents endless loops on malformed user JSON/validation.
     */
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 2000, multiplier = 2.0),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        include = { BusinessException.class, TimeoutException.class, HttpServerErrorException.class } 
    )
    @KafkaListener(topics = "ai-request", groupId = "smartcv-ai-group")
    @Transactional
    public void consumeAiRequest(String payload) {
        String jobId = extractFromJson(payload, "jobId");
        
        // ==========================================
        // 1. DB-LEVEL ATOMIC IDEMPOTENCY LOCK
        // Prevents Race conditions between concurrent partition listeners
        // ==========================================
        AiAnalysisJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("AI Job {} not found. Discarding phantom message.", jobId);
            return;
        }
        
        int rowsUpdated = jobRepository.lockJobForProcessing(jobId);
        if (rowsUpdated == 0) {
            log.warn("Atomic Race Condition intercepted! Job {} is already claimed by another worker. Dropping message.", jobId);
            return; // Successfully blocks Kafka replay duplications + thread race conditions
        }

        try {
            String cvContent = extractFromJson(payload, "cvContent");
            String userMessage = extractFromJson(payload, "prompt");

            String result = aiService.chatWithCv(cvContent, userMessage);
            
            job.setResult(result);
            job.setStatus(AiAnalysisJob.JobStatus.DONE);
            jobRepository.save(job);
            
            log.info("Finished async AI job {}", jobId);
        } catch (Exception e) {
            log.error("AI execution failed for job {}.", jobId);
            // Reset to PENDING so retry logic can pick it up on the next Kafka attempt
            job.setStatus(AiAnalysisJob.JobStatus.PENDING);
            jobRepository.save(job);
            throw e; // Pass to @RetryableTopic dispatcher
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
