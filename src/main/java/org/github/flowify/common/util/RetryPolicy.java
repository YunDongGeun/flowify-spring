package org.github.flowify.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class RetryPolicy {

    private final int maxRetries;
    private final long initialDelayMs;

    public RetryPolicy(int maxRetries, long initialDelayMs) {
        this.maxRetries = maxRetries;
        this.initialDelayMs = initialDelayMs;
    }

    public static RetryPolicy forExternalApi() {
        return new RetryPolicy(3, 1000);
    }

    public static RetryPolicy forLlmApi() {
        return new RetryPolicy(2, 2000);
    }

    public <T> T executeWithRetry(Supplier<T> action) {
        int attempt = 0;
        long delay = initialDelayMs;

        while (true) {
            try {
                return action.get();
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    log.error("최대 재시도 횟수({})를 초과했습니다.", maxRetries, e);
                    throw e;
                }
                log.warn("재시도 {}/{} - {}ms 후 재시도합니다. 오류: {}", attempt, maxRetries, delay, e.getMessage());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                }
                delay *= 2; // Exponential Backoff
            }
        }
    }
}
