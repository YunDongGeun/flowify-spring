package org.github.flowify.workflow.repository;

import org.github.flowify.workflow.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository extends MongoRepository<Workflow, String> {

    Page<Workflow> findByUserIdOrSharedWithContaining(String userId, String sharedUserId, Pageable pageable);

    List<Workflow> findByUserIdOrSharedWithContainingOrderByUpdatedAtDesc(String userId, String sharedUserId);

    List<Workflow> findByUserId(String userId);

    void deleteByUserId(String userId);

    List<Workflow> findByTrigger_TypeAndIsActive(String type, boolean active);

    @Query("{ 'trigger.config.webhookId': ?0 }")
    Optional<Workflow> findByWebhookId(String webhookId);
}