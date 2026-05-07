package com.example.SmartCV.modules.ai.worker;

import com.example.SmartCV.config.RedisStreamConfig;
import com.example.SmartCV.modules.ai.domain.AiAnalysisJob;
import com.example.SmartCV.modules.ai.repository.AiAnalysisJobRepository;
import com.example.SmartCV.modules.ai.service.AiGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final AiGateway aiGateway;
    private final AiAnalysisJobRepository jobRepository;
    private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    public void register() {
        // Create Consumer Group if it doesn't exist
        try {
            aiGateway.createStreamGroup(RedisStreamConfig.STREAM_AI_REQUEST, "ai-group");
        } catch (Exception e) {
            // Group might already exist
        }

        container.receive(
                Consumer.from("ai-group", "worker-1"),
                StreamOffset.create(RedisStreamConfig.STREAM_AI_REQUEST, ReadOffset.lastConsumed()),
                this
        );
        container.start();
        log.info("AI Stream Consumer registered for topic {}", RedisStreamConfig.STREAM_AI_REQUEST);
    }

    @Override
    @Transactional
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> payload = message.getValue();
        String jobId = payload.get("jobId");
        
        log.info("Processing AI Job from Stream: {}", jobId);

        AiAnalysisJob job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("AI Job {} not found.", jobId);
            return;
        }

        if (jobRepository.lockJobForProcessing(jobId) == 0) {
            return;
        }

        try {
            String cvContent = payload.get("cvContent");
            String prompt = payload.get("prompt");

            String result = aiGateway.chatWithCv(0L, cvContent, prompt, null, "en");
            
            job.setResult(result);
            job.setStatus(AiAnalysisJob.JobStatus.DONE);
            jobRepository.save(job);
            
            log.info("Finished async AI job {}", jobId);
        } catch (Exception e) {
            log.error("AI execution failed for job {}: {}", jobId, e.getMessage());
            job.setStatus(AiAnalysisJob.JobStatus.FAILED);
            jobRepository.save(job);
        }
    }
}
