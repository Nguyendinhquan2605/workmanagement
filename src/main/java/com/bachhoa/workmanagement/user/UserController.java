package com.bachhoa.workmanagement.user;

import com.bachhoa.workmanagement.user.dto.ChangeRoleRequest;
import com.bachhoa.workmanagement.user.dto.ChangeStatusRequest;
import com.bachhoa.workmanagement.user.dto.CreateUserRequest;
import com.bachhoa.workmanagement.user.dto.AssignableUserResponse;
import com.bachhoa.workmanagement.user.dto.UpdateMyProfileRequest;
import com.bachhoa.workmanagement.user.dto.UpdateUserRequest;
import com.bachhoa.workmanagement.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Quản lý tài khoản, vai trò, trạng thái và người nhận việc")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xem danh sách người dùng", description = "Chỉ ADMIN được xem toàn bộ danh sách tài khoản")
    public List<UserResponse> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Xem chi tiết người dùng", description = "ADMIN hoặc chính người dùng sở hữu tài khoản")
    public UserResponse getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo tài khoản người dùng")
    public ResponseEntity<UserResponse> create(
            @Valid @RequestBody CreateUserRequest request,
            UriComponentsBuilder uriBuilder) {
        UserResponse response = userService.create(request);
        URI location = uriBuilder.path("/api/users/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật tài khoản người dùng", description = "ADMIN cập nhật username và họ tên")
    public UserResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật hồ sơ cá nhân")
    public UserResponse updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateMyProfileRequest request) {
        return userService.updateMyProfile(authentication.getName(), request);
    }

    @GetMapping("/assignable")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Lấy danh sách MEMBER có thể nhận việc", description = "Chỉ trả về MEMBER đang ACTIVE")
    public List<AssignableUserResponse> getAssignableUsers() {
        return userService.getAssignableUsers();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thay đổi vai trò người dùng")
    public UserResponse changeRole(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeRoleRequest request) {
        return userService.changeRole(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Thay đổi trạng thái tài khoản")
    public UserResponse changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request) {
        return userService.changeStatus(id, request);
    }
}
