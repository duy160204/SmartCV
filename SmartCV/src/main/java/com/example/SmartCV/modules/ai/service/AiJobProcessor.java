package com.example.SmartCV.modules.ai.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.example.SmartCV.modules.ai.domain.AiAnalysisJob;
import com.example.SmartCV.modules.ai.repository.AiAnalysisJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiJobProcessor {

    private final AiAnalysisJobRepository aiAnalysisJobRepository;
    private final AiService aiService;

    /**
     * Executes the AI evaluation in a background thread and updates the database record.
     */
    @Async
    public void processJobAsync(String jobId, String cvContent, String prompt) {
        log.info("Starting async AI processing for job {}", jobId);
        
        AiAnalysisJob job = aiAnalysisJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Job {} not found in database", jobId);
            return;
        }

        job.setStatus(AiAnalysisJob.JobStatus.PROCESSING);
        aiAnalysisJobRepository.save(job);

        try {
            // Call the AI service to analyze the CV
            String result = aiService.chatWithCv(cvContent, prompt);
            job.setResult(result);
            job.setStatus(AiAnalysisJob.JobStatus.DONE);
            log.info("Async AI job {} completed successfully", jobId);
        } catch (Exception e) {
            log.error("Error processing async AI job {}: {}", jobId, e.getMessage());
            job.setStatus(AiAnalysisJob.JobStatus.FAILED);
        }

        aiAnalysisJobRepository.save(job);
    }
}
