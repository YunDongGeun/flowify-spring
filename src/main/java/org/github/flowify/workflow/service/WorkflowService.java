package org.github.flowify.workflow.service;

import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.PageResponse;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.workflow.dto.WorkflowCreateRequest;
import org.github.flowify.workflow.dto.WorkflowResponse;
import org.github.flowify.workflow.dto.WorkflowUpdateRequest;
import org.github.flowify.workflow.entity.Workflow;
import org.github.flowify.workflow.repository.WorkflowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowValidator workflowValidator;

    public WorkflowResponse createWorkflow(String userId, WorkflowCreateRequest request) {
        Workflow workflow = Workflow.builder()
                .name(request.getName())
                .description(request.getDescription())
                .userId(userId)
                .nodes(request.getNodes() != null ? request.getNodes() : new ArrayList<>())
                .edges(request.getEdges() != null ? request.getEdges() : new ArrayList<>())
                .trigger(request.getTrigger())
                .build();

        workflowValidator.validate(workflow);
        Workflow saved = workflowRepository.save(workflow);
        return WorkflowResponse.from(saved);
    }

    public PageResponse<WorkflowResponse> getWorkflowsByUserId(String userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Workflow> workflows = workflowRepository.findByUserIdOrSharedWithContaining(userId, userId, pageRequest);

        List<WorkflowResponse> content = workflows.getContent().stream()
                .map(WorkflowResponse::from)
                .toList();

        return PageResponse.of(content, page, size, workflows.getTotalElements());
    }

    public WorkflowResponse getWorkflowById(String userId, String workflowId) {
        Workflow workflow = findWorkflowOrThrow(workflowId);
        verifyAccess(workflow, userId);
        return WorkflowResponse.from(workflow);
    }

    public WorkflowResponse updateWorkflow(String userId, String workflowId, WorkflowUpdateRequest request) {
        Workflow workflow = findWorkflowOrThrow(workflowId);
        verifyAccess(workflow, userId);

        if (request.getName() != null) {
            workflow.setName(request.getName());
        }
        if (request.getDescription() != null) {
            workflow.setDescription(request.getDescription());
        }
        if (request.getNodes() != null) {
            workflow.setNodes(request.getNodes());
        }
        if (request.getEdges() != null) {
            workflow.setEdges(request.getEdges());
        }
        if (request.getTrigger() != null) {
            workflow.setTrigger(request.getTrigger());
        }
        if (request.getIsActive() != null) {
            workflow.setActive(request.getIsActive());
        }

        workflowValidator.validate(workflow);
        Workflow saved = workflowRepository.save(workflow);
        return WorkflowResponse.from(saved);
    }

    public void deleteWorkflow(String userId, String workflowId) {
        Workflow workflow = findWorkflowOrThrow(workflowId);
        verifyOwnership(workflow, userId);
        workflowRepository.delete(workflow);
    }

    public void shareWorkflow(String userId, String workflowId, List<String> userIds) {
        Workflow workflow = findWorkflowOrThrow(workflowId);
        verifyOwnership(workflow, userId);
        workflow.setSharedWith(userIds);
        workflowRepository.save(workflow);
    }

    public Workflow findWorkflowOrThrow(String workflowId) {
        return workflowRepository.findById(workflowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKFLOW_NOT_FOUND));
    }

    private void verifyOwnership(Workflow workflow, String userId) {
        if (!workflow.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }
    }

    private void verifyAccess(Workflow workflow, String userId) {
        if (!workflow.getUserId().equals(userId)
                && !workflow.getSharedWith().contains(userId)) {
            throw new BusinessException(ErrorCode.WORKFLOW_ACCESS_DENIED);
        }
    }
}
