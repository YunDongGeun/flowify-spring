package org.github.flowify.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.oauth.entity.OAuthToken;
import org.github.flowify.oauth.repository.OAuthTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthTokenService {

    private static final long REFRESH_THRESHOLD_SECONDS = 300; // 5분

    private final OAuthTokenRepository oauthTokenRepository;
    private final TokenEncryptionService tokenEncryptionService;

    public List<Map<String, Object>> getConnectedServices(String userId) {
        return oauthTokenRepository.findByUserId(userId).stream()
                .map(token -> Map.<String, Object>of(
                        "service", token.getService(),
                        "connected", true,
                        "expiresAt", token.getExpiresAt() != null ? token.getExpiresAt().toString() : ""
                ))
                .toList();
    }

    public void saveToken(String userId, String service, String accessToken,
                          String refreshToken, Instant expiresAt, List<String> scopes) {
        OAuthToken oauthToken = oauthTokenRepository.findByUserIdAndService(userId, service)
                .orElse(OAuthToken.builder()
                        .userId(userId)
                        .service(service)
                        .build());

        oauthToken.setAccessToken(tokenEncryptionService.encrypt(accessToken));
        if (refreshToken != null) {
            oauthToken.setRefreshToken(tokenEncryptionService.encrypt(refreshToken));
        }
        oauthToken.setExpiresAt(expiresAt);
        oauthToken.setScopes(scopes);

        oauthTokenRepository.save(oauthToken);
    }

    public String getDecryptedToken(String userId, String service) {
        OAuthToken token = oauthTokenRepository.findByUserIdAndService(userId, service)
                .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_NOT_CONNECTED));

        if (isTokenExpiringSoon(token)) {
            refreshTokenIfNeeded(token);
        }

        return tokenEncryptionService.decrypt(token.getAccessToken());
    }

    public void refreshTokenIfNeeded(OAuthToken token) {
        if (token.getRefreshToken() == null) {
            throw new BusinessException(ErrorCode.OAUTH_TOKEN_EXPIRED);
        }

        // 실제 OAuth refresh 로직은 서비스별로 다르므로 여기서는 placeholder
        // 각 서비스(Google, Slack, Notion)의 token refresh endpoint 호출 필요
        log.warn("토큰 자동 갱신이 필요합니다: userId={}, service={}", token.getUserId(), token.getService());
    }

    public void deleteToken(String userId, String service) {
        oauthTokenRepository.deleteByUserIdAndService(userId, service);
    }

    private boolean isTokenExpiringSoon(OAuthToken token) {
        if (token.getExpiresAt() == null) {
            return false;
        }
        return Instant.now().plusSeconds(REFRESH_THRESHOLD_SECONDS).isAfter(token.getExpiresAt());
    }
}
