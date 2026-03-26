package org.github.flowify.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.github.flowify.user.entity.User;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {

    private final String id;
    private final String email;
    private final String name;
    private final String picture;
    private final Instant createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
