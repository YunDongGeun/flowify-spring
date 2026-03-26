package org.github.flowify.oauth.controller;

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

@RestController
@RequestMapping("/api/oauth-tokens")
@RequiredArgsConstructor
public class OAuthTokenController {

    private final OAuthTokenService oauthTokenService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getConnectedServices(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(oauthTokenService.getConnectedServices(user.getId()));
    }

    @PostMapping("/{service}/connect")
    public ApiResponse<Map<String, String>> connectService(Authentication authentication,
                                                           @PathVariable String service) {
        // OAuth 인증 URL 생성 (서비스별 분기)
        // 실제 구현에서는 서비스별 OAuth URL을 생성하여 반환
        String authUrl = buildOAuthUrl(service);
        return ApiResponse.ok(Map.of("authUrl", authUrl));
    }

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
