package org.github.flowify.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "사용자", description = "사용자 정보 관리")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(userService.getUserById(user.getId()));
    }

    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(Authentication authentication,
                                              @RequestBody UserUpdateRequest request) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.ok(userService.updateUser(user.getId(), request));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴 및 관련 데이터를 일괄 삭제합니다.")
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        userService.deleteUser(user.getId());
        return ApiResponse.ok();
    }
}
