package org.github.flowify.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.github.flowify.auth.dto.LoginResponse;
import org.github.flowify.auth.dto.TokenRefreshRequest;
import org.github.flowify.auth.service.AuthService;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.user.entity.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/google")
    public ResponseEntity<Void> googleLogin(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        String googleLoginUrl = authService.getGoogleLoginUrl(baseUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, googleLoginUrl)
                .build();
    }

    @GetMapping("/google/callback")
    public ApiResponse<LoginResponse> googleCallback(@RequestParam String code,
                                                     HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        LoginResponse loginResponse = authService.processGoogleLogin(code, baseUrl);
        return ApiResponse.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        LoginResponse loginResponse = authService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        authService.logout(user.getId());
        return ApiResponse.ok();
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        if ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443)) {
            return scheme + "://" + serverName;
        }
        return scheme + "://" + serverName + ":" + serverPort;
    }
}