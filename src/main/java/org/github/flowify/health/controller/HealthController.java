package org.github.flowify.health.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.github.flowify.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "시스템", description = "서버 상태 확인")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "헬스체크", description = "서버 상태 및 타임스탬프를 반환합니다.")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "service", "Flowify Spring Boot",
                "timestamp", Instant.now().toString()
        );
        return ApiResponse.ok(data);
    }
}
