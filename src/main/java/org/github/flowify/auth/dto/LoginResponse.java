package org.github.flowify.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.github.flowify.user.dto.UserResponse;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private final String accessToken;
    private final String refreshToken;
    private final UserResponse user;
}