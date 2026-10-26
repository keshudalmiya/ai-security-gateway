package com.security.gateway.repository;

import com.security.gateway.domain.SecurityRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityRuleRepository extends MongoRepository<SecurityRule, String> {
    Optional<SecurityRule> findByRuleId(String ruleId);
    List<SecurityRule> findAllByOrderByRuleIdAsc();
}
