package com.security.gateway.repository;

import com.security.gateway.domain.EvaluationRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EvaluationRunRepository extends MongoRepository<EvaluationRun, String> {
    Optional<EvaluationRun> findTopByOrderByCreatedAtDesc();
}
