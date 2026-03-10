package org.github.flowify.health.controller;

import org.github.flowify.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

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
