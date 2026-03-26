package org.github.flowify.user.controller;

import lombok.RequiredArgsConstructor;
import org.github.flowify.common.dto.ApiResponse;
import org.github.flowify.user.dto.UserResponse;
import org.github.flowify.user.dto.UserUpdateRequest;
import org.github.flowify.user.entity.User;
import org.github.flowify.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(userService.getUserById(user.getId()));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(Authentication authentication,
                                              @RequestBody UserUpdateRequest request) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(userService.updateUser(user.getId(), request));
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userService.deleteUser(user.getId());
        return ApiResponse.ok();
    }
}
