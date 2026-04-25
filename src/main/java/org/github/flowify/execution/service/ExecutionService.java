package org.github.flowify.execution.service;

import lombok.RequiredArgsConstructor;
import org.github.flowify.catalog.service.CatalogService;
import org.github.flowify.catalog.service.NodeLifecycleService;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.execution.entity.WorkflowExecution;
import org.github.flowify.execution.repository.ExecutionRepository;
import org.github.flowify.oauth.service.OAuthTokenService;
import org.github.flowify.workflow.entity.NodeDefinition;
import org.github.flowify.workflow.entity.Workflow;
import org.github.flowify.workflow.service.WorkflowService;
import org.github.flowify.workflow.service.WorkflowValidator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ExecutionService {

    private final ExecutionRepository executionRepository;
    private final WorkflowService workflowService;
    private final MongoTemplate mongoTemplate;
    private final FastApiClient fastApiClient;
    private final OAuthTokenService oauthTokenService;
    private final CatalogService catalogService;
    private final NodeLifecycleService nodeLifecycleService;
    private final SnapshotService snapshotService;
    private final WorkflowValidator workflowValidator;
    private final WorkflowTranslator workflowTranslator;

    public String executeWorkflow(String userId, String workflowId) {
        Workflow workflow = workflowService.findWorkflowOrThrow(workflowId);

        if (!workflow.getUserId().equals(userId)
                && !workflow.getSharedWith().contains(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        workflowValidator.validateForExecution(workflow, nodeLifecycleService, catalogService, userId);

        Map<String, String> serviceTokens = collectServiceTokens(userId, workflow.getNodes());

        Map<String, Object> runtimeModel = workflowTranslator.toRuntimeModel(workflow);
        return fastApiClient.execute(workflowId, userId, runtimeModel, serviceTokens);
    }

    public List<WorkflowExecution> getExecutionsByWorkflowId(String userId, String workflowId) {
        Workflow workflow = workflowService.findWorkflowOrThrow(workflowId);

        if (!workflow.getUserId().equals(userId)
                && !workflow.getSharedWith().contains(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        return executionRepository.findByWorkflowId(workflowId);
    }

    public WorkflowExecution getExecutionDetail(String userId, String executionId) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXECUTION_NOT_FOUND));

        if (!execution.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        return execution;
    }

    public void stopExecution(String userId, String executionId) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXECUTION_NOT_FOUND));

        if (!execution.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        if (!"running".equals(execution.getState())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "실행 중인 워크플로우만 중지할 수 있습니다.");
        }

        fastApiClient.stopExecution(executionId, userId);
    }

    public void rollbackExecution(String userId, String executionId, String nodeId) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXECUTION_NOT_FOUND));

        if (!execution.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }

        snapshotService.rollbackToSnapshot(userId, executionId, nodeId);
    }

    public String executeScheduled(String workflowId) {
        Workflow workflow = workflowService.findWorkflowOrThrow(workflowId);
        String userId = workflow.getUserId();
        Map<String, String> tokens = collectServiceTokens(userId, workflow.getNodes());
        Map<String, Object> runtimeModel = workflowTranslator.toRuntimeModel(workflow);
        return fastApiClient.execute(workflowId, userId, runtimeModel, tokens);
    }

    @SuppressWarnings("unchecked")
    public String executeFromWebhook(String workflowId, Map<String, Object> eventPayload) {
        Workflow workflow = workflowService.findWorkflowOrThrow(workflowId);
        String userId = workflow.getUserId();
        Map<String, String> tokens = collectServiceTokens(userId, workflow.getNodes());
        Map<String, Object> runtimeModel = workflowTranslator.toRuntimeModel(workflow);

        Map<String, Object> triggerSection = (Map<String, Object>) runtimeModel.get("trigger");
        if (triggerSection != null) {
            triggerSection.computeIfAbsent("config", k -> new HashMap<>());
            ((Map<String, Object>) triggerSection.get("config")).put("event_payload", eventPayload);
        }

        return fastApiClient.execute(workflowId, userId, runtimeModel, tokens);
    }

    public void completeExecution(String execId, String status, String error) {
        Query query = Query.query(Criteria.where("_id").is(execId));
        Update update = new Update()
                .set("state", status)
                .set("finishedAt", Instant.now());

        long matched = mongoTemplate.updateFirst(query, update, WorkflowExecution.class).getMatchedCount();
        if (matched == 0) {
            throw new BusinessException(ErrorCode.EXECUTION_NOT_FOUND);
        }
    }

    private Map<String, String> collectServiceTokens(String userId, List<NodeDefinition> nodes) {
        Map<String, String> tokens = new HashMap<>();

        nodes.stream()
                .map(NodeDefinition::getType)
                .filter(Objects::nonNull)
                .distinct()
                .filter(catalogService::isAuthRequired)
                .forEach(service -> {
                    try {
                        String token = oauthTokenService.getDecryptedToken(userId, service);
                        tokens.put(service, token);
                    } catch (BusinessException e) {
                        throw new BusinessException(ErrorCode.OAUTH_NOT_CONNECTED,
                                service + " 서비스가 연결되지 않았습니다.");
                    }
                });

        return tokens;
    }
}
