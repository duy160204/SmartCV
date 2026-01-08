package com.example.SmartCV.modules.cv.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartCV.modules.cv.domain.CVShare;

public interface CVShareRepository extends JpaRepository<CVShare, Long> {

    Optional<CVShare> findByShareUuid(String shareUuid);

    boolean existsByCvId(Long cvId);

    void deleteByCvId(Long cvId);
}
