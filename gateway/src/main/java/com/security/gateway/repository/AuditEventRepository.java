package com.security.gateway.repository;

import com.security.gateway.domain.AuditEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends MongoRepository<AuditEvent, String> {
    List<AuditEvent> findTop50ByOrderByCreatedAtDesc();
}
