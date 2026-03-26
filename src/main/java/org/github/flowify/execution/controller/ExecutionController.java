package org.github.flowify.execution.controller;

import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.execution.entity.WorkflowExecution;
import org.github.flowify.execution.service.ExecutionService;
import org.github.flowify.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    @PostMapping("/{id}/execute")
    public ApiResponse<String> executeWorkflow(Authentication authentication,
                                               @PathVariable String id) {
        User user = (User) authentication.getPrincipal();
        String executionId = executionService.executeWorkflow(user.getId(), id);
        return ApiResponse.ok(executionId);
    }

    @GetMapping("/{id}/executions")
    public ApiResponse<List<WorkflowExecution>> getExecutions(Authentication authentication,
                                                              @PathVariable String id) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(executionService.getExecutionsByWorkflowId(user.getId(), id));
    }

    @GetMapping("/{id}/executions/{execId}")
    public ApiResponse<WorkflowExecution> getExecutionDetail(Authentication authentication,
                                                             @PathVariable String id,
                                                             @PathVariable String execId) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(executionService.getExecutionDetail(user.getId(), execId));
    }

    @PostMapping("/{id}/executions/{execId}/rollback")
    public ApiResponse<Void> rollbackExecution(Authentication authentication,
                                               @PathVariable String id,
                                               @PathVariable String execId,
                                               @RequestParam(required = false) String nodeId) {
        User user = (User) authentication.getPrincipal();
        executionService.rollbackExecution(user.getId(), execId, nodeId);
        return ApiResponse.ok();
    }
}
