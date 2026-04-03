package com.example.SmartCV.modules.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.ai.domain.AiAnalysisJob;

@Repository
public interface AiAnalysisJobRepository extends JpaRepository<AiAnalysisJob, String> {
    
    /**
     * ATOMIC UPDATE: Prevents Kafka Consumer Race Conditions.
     * Only 1 consumer thread can successfully change the status from PENDING to PROCESSING.
     */
    @Modifying
    @Transactional
    @Query("UPDATE AiAnalysisJob j SET j.status = 'PROCESSING' WHERE j.jobId = :jobId AND j.status = 'PENDING'")
    int lockJobForProcessing(@Param("jobId") String jobId);
}
