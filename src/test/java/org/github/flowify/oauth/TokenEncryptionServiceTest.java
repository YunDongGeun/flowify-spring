package org.github.flowify.oauth;

import org.github.flowify.oauth.service.TokenEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenEncryptionServiceTest {

    private TokenEncryptionService encryptionService;
    private TokenEncryptionService otherEncryptionService;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // AES-256 키 생성
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);

        String key1 = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
        String key2 = Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());

        encryptionService = new TokenEncryptionService(key1);
        otherEncryptionService = new TokenEncryptionService(key2);
    }

    @Test
    @DisplayName("암호화 후 복호화 라운드트립 성공")
    void encryptDecryptRoundTrip() {
        String plaintext = "ya29.a0AfH6SMA_test_access_token_12345";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("동일한 평문을 암호화해도 결과가 다름 (IV 랜덤)")
    void encryptProducesDifferentCiphertexts() {
        String plaintext = "test-token";

        String encrypted1 = encryptionService.encrypt(plaintext);
        String encrypted2 = encryptionService.encrypt(plaintext);

        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    @DisplayName("암호화된 텍스트는 원본과 다름")
    void encryptedTextDiffersFromPlaintext() {
        String plaintext = "my-secret-token";

        String encrypted = encryptionService.encrypt(plaintext);

        assertThat(encrypted).isNotEqualTo(plaintext);
    }

    @Test
    @DisplayName("다른 키로 복호화 시 실패")
    void decryptWithDifferentKeyFails() {
        String plaintext = "test-token";
        String encrypted = encryptionService.encrypt(plaintext);

        assertThatThrownBy(() -> otherEncryptionService.decrypt(encrypted))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("한글 텍스트 암호화/복호화 성공")
    void encryptDecryptKoreanText() {
        String plaintext = "한글토큰테스트";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("긴 텍스트 암호화/복호화 성공")
    void encryptDecryptLongText() {
        String plaintext = "a".repeat(1000);

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }

    @Test
    @DisplayName("빈 문자열 암호화/복호화 성공")
    void encryptDecryptEmptyString() {
        String plaintext = "";

        String encrypted = encryptionService.encrypt(plaintext);
        String decrypted = encryptionService.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(plaintext);
    }
}
