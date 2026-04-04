package org.github.flowify.auth.jwt;

import org.github.flowify.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private User testUser;

    // 테스트용 시크릿 (256비트 이상)
    private static final String TEST_SECRET = "flowify-test-jwt-secret-key-must-be-at-least-256-bits-long!!";
    private static final long ACCESS_EXPIRATION_MS = 1800000; // 30분
    private static final long REFRESH_EXPIRATION_MS = 604800000; // 7일

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(TEST_SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);
        testUser = User.builder()
                .id("user123")
                .email("test@gmail.com")
                .name("테스트유저")
                .googleId("google123")
                .build();
    }

    @Test
    @DisplayName("Access Token 생성 및 검증 성공")
    void generateAndValidateAccessToken() {
        String token = jwtProvider.generateAccessToken(testUser);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 생성 및 검증 성공")
    void generateAndValidateRefreshToken() {
        String token = jwtProvider.generateRefreshToken(testUser);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출")
    void getUserIdFromToken() {
        String token = jwtProvider.generateAccessToken(testUser);

        String userId = jwtProvider.getUserIdFromToken(token);

        assertThat(userId).isEqualTo("user123");
    }

    @Test
    @DisplayName("토큰에서 이메일 추출")
    void getEmailFromToken() {
        String token = jwtProvider.generateAccessToken(testUser);

        String email = jwtProvider.getEmailFromToken(token);

        assertThat(email).isEqualTo("test@gmail.com");
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 검증 실패")
    void validateTokenWithWrongSignature() {
        JwtProvider otherProvider = new JwtProvider(
                "another-secret-key-that-is-at-least-256-bits-long-for-testing!!",
                ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS);

        String token = otherProvider.generateAccessToken(testUser);

        assertThat(jwtProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateExpiredToken() {
        JwtProvider shortLivedProvider = new JwtProvider(TEST_SECRET, 0, 0);

        String token = shortLivedProvider.generateAccessToken(testUser);

        assertThat(jwtProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 isTokenExpired 확인")
    void isTokenExpiredReturnsTrue() {
        JwtProvider shortLivedProvider = new JwtProvider(TEST_SECRET, 0, 0);

        String token = shortLivedProvider.generateAccessToken(testUser);

        assertThat(jwtProvider.isTokenExpired(token)).isTrue();
    }

    @Test
    @DisplayName("유효한 토큰은 만료되지 않음")
    void isTokenExpiredReturnsFalse() {
        String token = jwtProvider.generateAccessToken(testUser);

        assertThat(jwtProvider.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("null 토큰 검증 실패")
    void validateNullToken() {
        assertThat(jwtProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰 검증 실패")
    void validateEmptyToken() {
        assertThat(jwtProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("형식이 잘못된 토큰 검증 실패")
    void validateMalformedToken() {
        assertThat(jwtProvider.validateToken("not.a.valid.jwt.token")).isFalse();
    }
}
