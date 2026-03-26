package org.github.flowify.execution.service;

import lombok.RequiredArgsConstructor;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.execution.entity.WorkflowExecution;
import org.github.flowify.execution.repository.ExecutionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final ExecutionRepository executionRepository;
    private final FastApiClient fastApiClient;

    public void rollbackToSnapshot(String userId, String executionId, String nodeId) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXECUTION_NOT_FOUND));

        if (!"rollback_available".equals(execution.getState()) && !"failed".equals(execution.getState())) {
            throw new BusinessException(ErrorCode.EXECUTION_FAILED, "롤백할 수 없는 상태입니다.");
        }

        fastApiClient.rollback(executionId, nodeId, userId);
    }
}
