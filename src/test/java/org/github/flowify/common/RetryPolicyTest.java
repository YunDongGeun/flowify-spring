package org.github.flowify.common;

import org.github.flowify.common.util.RetryPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryPolicyTest {

    @Test
    @DisplayName("성공 시 즉시 결과 반환")
    void executeWithRetry_successOnFirstAttempt() {
        RetryPolicy policy = new RetryPolicy(3, 10);

        String result = policy.executeWithRetry(() -> "success");

        assertThat(result).isEqualTo("success");
    }

    @Test
    @DisplayName("재시도 후 성공")
    void executeWithRetry_successAfterRetries() {
        RetryPolicy policy = new RetryPolicy(3, 10);
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.executeWithRetry(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("일시적 오류");
            }
            return "success";
        });

        assertThat(result).isEqualTo("success");
        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("최대 재시도 횟수 초과 시 예외 전파")
    void executeWithRetry_exceedsMaxRetries() {
        RetryPolicy policy = new RetryPolicy(3, 10);

        assertThatThrownBy(() ->
                policy.executeWithRetry(() -> {
                    throw new RuntimeException("항상 실패");
                })
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("항상 실패");
    }

    @Test
    @DisplayName("forExternalApi 팩토리 메서드: 3회 재시도")
    void forExternalApi_retries3Times() {
        RetryPolicy policy = RetryPolicy.forExternalApi();
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() ->
                policy.executeWithRetry(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("API 오류");
                })
        ).isInstanceOf(RuntimeException.class);

        assertThat(attempts.get()).isEqualTo(3);
    }

    @Test
    @DisplayName("forLlmApi 팩토리 메서드: 2회 재시도")
    void forLlmApi_retries2Times() {
        RetryPolicy policy = RetryPolicy.forLlmApi();
        AtomicInteger attempts = new AtomicInteger(0);

        assertThatThrownBy(() ->
                policy.executeWithRetry(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("LLM 오류");
                })
        ).isInstanceOf(RuntimeException.class);

        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("첫 번째 실패 후 두 번째 성공")
    void executeWithRetry_failOnceSucceedNext() {
        RetryPolicy policy = new RetryPolicy(3, 10);
        AtomicInteger attempts = new AtomicInteger(0);

        String result = policy.executeWithRetry(() -> {
            if (attempts.incrementAndGet() == 1) {
                throw new RuntimeException("첫 번째 시도 실패");
            }
            return "recovered";
        });

        assertThat(result).isEqualTo("recovered");
        assertThat(attempts.get()).isEqualTo(2);
    }
}
