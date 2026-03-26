package org.github.flowify.user.service;

import lombok.RequiredArgsConstructor;
import org.github.flowify.common.exception.BusinessException;
import org.github.flowify.common.exception.ErrorCode;
import org.github.flowify.execution.repository.ExecutionRepository;
import org.github.flowify.oauth.repository.OAuthTokenRepository;
import org.github.flowify.user.dto.UserResponse;
import org.github.flowify.user.dto.UserUpdateRequest;
import org.github.flowify.user.entity.User;
import org.github.flowify.user.repository.UserRepository;
import org.github.flowify.workflow.repository.WorkflowRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository;
    private final OAuthTokenRepository oauthTokenRepository;
    private final ExecutionRepository executionRepository;

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    public UserResponse updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public void deleteUser(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        executionRepository.deleteByUserId(userId);
        oauthTokenRepository.deleteByUserId(userId);
        workflowRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
