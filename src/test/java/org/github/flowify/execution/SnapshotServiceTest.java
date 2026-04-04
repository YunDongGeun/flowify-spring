package org.github.flowify.execution;

import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.execution.entity.WorkflowExecution;
import org.github.flowify.execution.repository.ExecutionRepository;
import org.github.flowify.execution.service.FastApiClient;
import org.github.flowify.execution.service.SnapshotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private ExecutionRepository executionRepository;
    @Mock
    private FastApiClient fastApiClient;

    @InjectMocks
    private SnapshotService snapshotService;

    @Test
    @DisplayName("rollback_available 상태에서 롤백 성공")
    void rollbackToSnapshot_rollbackAvailable() {
        WorkflowExecution execution = WorkflowExecution.builder()
                .id("exec1").userId("user123").state("rollback_available").build();
        when(executionRepository.findById("exec1")).thenReturn(Optional.of(execution));

        snapshotService.rollbackToSnapshot("user123", "exec1", "node_1");

        verify(fastApiClient).rollback("exec1", "node_1", "user123");
    }

    @Test
    @DisplayName("failed 상태에서 롤백 성공")
    void rollbackToSnapshot_failedState() {
        WorkflowExecution execution = WorkflowExecution.builder()
                .id("exec1").userId("user123").state("failed").build();
        when(executionRepository.findById("exec1")).thenReturn(Optional.of(execution));

        snapshotService.rollbackToSnapshot("user123", "exec1", "node_1");

        verify(fastApiClient).rollback("exec1", "node_1", "user123");
    }

    @Test
    @DisplayName("success 상태에서 롤백 불가")
    void rollbackToSnapshot_invalidState() {
        WorkflowExecution execution = WorkflowExecution.builder()
                .id("exec1").userId("user123").state("success").build();
        when(executionRepository.findById("exec1")).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> snapshotService.rollbackToSnapshot("user123", "exec1", "node_1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("롤백할 수 없는 상태");
    }

    @Test
    @DisplayName("running 상태에서 롤백 불가")
    void rollbackToSnapshot_runningState() {
        WorkflowExecution execution = WorkflowExecution.builder()
                .id("exec1").userId("user123").state("running").build();
        when(executionRepository.findById("exec1")).thenReturn(Optional.of(execution));

        assertThatThrownBy(() -> snapshotService.rollbackToSnapshot("user123", "exec1", "node_1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXECUTION_FAILED);
    }

    @Test
    @DisplayName("존재하지 않는 실행에 대한 롤백")
    void rollbackToSnapshot_executionNotFound() {
        when(executionRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> snapshotService.rollbackToSnapshot("user123", "unknown", "node_1"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXECUTION_NOT_FOUND);
    }
}
