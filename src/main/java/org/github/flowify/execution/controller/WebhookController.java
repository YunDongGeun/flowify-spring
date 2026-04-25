package org.github.flowify.execution.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.execution.dto.WebhookIssuedResponse;
import org.github.flowify.execution.service.WebhookService;
import org.github.flowify.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "웹훅 관리", description = "워크플로우 웹훅 발급 및 관리")
@RestController
@RequestMapping("/api/workflows/{id}/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;

    @Operation(summary = "웹훅 발급", description = "워크플로우에 웹훅 ID와 서명 검증용 시크릿을 발급합니다.")
    @PostMapping
    public ApiResponse<WebhookIssuedResponse> issueWebhook(Authentication authentication,
                                                            @PathVariable String id) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(webhookService.issueWebhook(user.getId(), id));
    }

    @Operation(summary = "웹훅 무효화", description = "발급된 웹훅을 무효화합니다.")
    @DeleteMapping
    public ApiResponse<Void> revokeWebhook(Authentication authentication,
                                            @PathVariable String id) {
        User user = (User) authentication.getPrincipal();
        webhookService.revokeWebhook(user.getId(), id);
        return ApiResponse.ok();
    }

    @Operation(summary = "웹훅 정보 조회", description = "현재 발급된 웹훅 ID를 조회합니다. (시크릿 미포함)")
    @GetMapping
    public ApiResponse<WebhookIssuedResponse> getWebhookInfo(Authentication authentication,
                                                              @PathVariable String id) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(webhookService.getWebhookInfo(user.getId(), id));
    }
}
