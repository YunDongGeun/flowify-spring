package org.github.flowify.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.oauth.service.OAuthTokenService;
import org.github.flowify.user.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Tag(name = "OAuth 토큰", description = "외부 서비스 OAuth 연동 관리")
@RestController
@RequestMapping("/api/oauth-tokens")
@RequiredArgsConstructor
public class OAuthTokenController {

    private final OAuthTokenService oauthTokenService;

    @Operation(summary = "연결된 서비스 목록 조회", description = "현재 사용자가 연결한 외부 서비스 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getConnectedServices(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(oauthTokenService.getConnectedServices(user.getId()));
    }

    @Operation(summary = "외부 서비스 연결", description = "외부 서비스 OAuth 인증을 시작합니다.")
    @PostMapping("/{service}/connect")
    public ApiResponse<Map<String, String>> connectService(Authentication authentication,
                                                           @PathVariable String service) {
        // OAuth 인증 URL 생성 (서비스별 분기)
        // 실제 구현에서는 서비스별 OAuth URL을 생성하여 반환
        String authUrl = buildOAuthUrl(service);
        return ApiResponse.ok(Map.of("authUrl", authUrl));
    }

    @Operation(summary = "OAuth 콜백", description = "외부 서비스 인증 후 토큰을 저장합니다.")
    @GetMapping("/{service}/callback")
    public ApiResponse<Void> oauthCallback(@PathVariable String service,
                                           @RequestParam String code,
                                           Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // 실제 구현에서는 code를 이용해 토큰 교환 후 저장
        // 서비스별 token exchange 로직 필요
        oauthTokenService.saveToken(user.getId(), service,
                "placeholder_access_token", "placeholder_refresh_token",
                Instant.now().plusSeconds(3600), List.of());
        return ApiResponse.ok();
    }

    @Operation(summary = "서비스 연결 해제", description = "외부 서비스 연동을 해제하고 토큰을 삭제합니다.")
    @DeleteMapping("/{service}")
    public ApiResponse<Void> disconnectService(Authentication authentication,
                                               @PathVariable String service) {
        User user = (User) authentication.getPrincipal();
        oauthTokenService.deleteToken(user.getId(), service);
        return ApiResponse.ok();
    }

    private String buildOAuthUrl(String service) {
        // 서비스별 OAuth URL 생성 로직
        // Google, Slack, Notion 각각의 OAuth authorize URL 반환
        return "https://" + service + ".com/oauth/authorize";
    }
}
