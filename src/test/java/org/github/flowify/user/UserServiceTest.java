package org.github.flowify.user;

import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.execution.repository.ExecutionRepository;
import org.github.flowify.oauth.repository.OAuthTokenRepository;
import org.github.flowify.user.dto.UserResponse;
import org.github.flowify.user.dto.UserUpdateRequest;
import org.github.flowify.user.entity.User;
import org.github.flowify.user.repository.UserRepository;
import org.github.flowify.user.service.UserService;
import org.github.flowify.workflow.repository.WorkflowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkflowRepository workflowRepository;
    @Mock
    private OAuthTokenRepository oauthTokenRepository;
    @Mock
    private ExecutionRepository executionRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id("user123")
                .email("test@gmail.com")
                .name("테스트유저")
                .picture("https://example.com/photo.jpg")
                .googleId("google123")
                .build();
    }

    @Test
    @DisplayName("사용자 조회 성공")
    void getUserById_success() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        UserResponse response = userService.getUserById("user123");

        assertThat(response.getId()).isEqualTo("user123");
        assertThat(response.getEmail()).isEqualTo("test@gmail.com");
        assertThat(response.getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생")
    void getUserById_notFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("unknown"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 이름 수정 성공")
    void updateUser_success() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserUpdateRequest request = new UserUpdateRequest();
        // UserUpdateRequest에는 setter가 없으므로 ObjectMapper 사용
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        UserUpdateRequest updateRequest = mapper.convertValue(
                java.util.Map.of("name", "새이름"), UserUpdateRequest.class);

        UserResponse response = userService.updateUser("user123", updateRequest);

        assertThat(response.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("사용자 삭제 시 관련 데이터 일괄 삭제")
    void deleteUser_cascadeDelete() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        userService.deleteUser("user123");

        verify(executionRepository).deleteByUserId("user123");
        verify(oauthTokenRepository).deleteByUserId("user123");
        verify(workflowRepository).deleteByUserId("user123");
        verify(userRepository).deleteById("user123");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 예외 발생")
    void deleteUser_notFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("unknown"))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
