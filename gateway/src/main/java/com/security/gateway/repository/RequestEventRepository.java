package com.security.gateway.repository;

import com.security.gateway.domain.Decision;
import com.security.gateway.domain.RequestEvent;
import com.security.gateway.domain.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RequestEventRepository extends MongoRepository<RequestEvent, String> {
    Optional<RequestEvent> findByRequestId(String requestId);
    Page<RequestEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<RequestEvent> findByDecisionOrderByCreatedAtDesc(Decision decision, Pageable pageable);
    Page<RequestEvent> findByRiskLevelOrderByCreatedAtDesc(RiskLevel riskLevel, Pageable pageable);
    long countByDecision(Decision decision);
    long countByRiskLevel(RiskLevel riskLevel);
}
