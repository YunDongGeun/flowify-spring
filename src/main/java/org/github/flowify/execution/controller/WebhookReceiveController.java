package org.github.flowify.execution.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.execution.service.WebhookService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "웹훅 수신", description = "외부 서비스로부터 웹훅 이벤트 수신 (인증 불필요)")
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookReceiveController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "웹훅 이벤트 수신",
            description = "외부 서비스가 POST로 이벤트를 전달합니다. X-Hub-Signature-256 헤더로 서명을 검증합니다.")
    @PostMapping("/{webhookId}")
    public ApiResponse<String> receiveWebhook(
            @PathVariable String webhookId,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String rawBody) {

        Map<String, Object> payload;
        try {
            payload = objectMapper.readValue(rawBody, new TypeReference<>() {});
        } catch (Exception e) {
            payload = Map.of("raw", rawBody);
        }

        String executionId = webhookService.processWebhook(webhookId, signature, rawBody, payload);
        return ApiResponse.ok(executionId);
    }
}
