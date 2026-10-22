package com.bachhoa.workmanagement.user;

import com.bachhoa.workmanagement.common.exception.ConflictException;
import com.bachhoa.workmanagement.common.exception.ResourceNotFoundException;
import com.bachhoa.workmanagement.entity.Role;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.UserStatus;
import com.bachhoa.workmanagement.repository.RoleRepository;
import com.bachhoa.workmanagement.repository.UserRepository;
import com.bachhoa.workmanagement.user.dto.ChangeRoleRequest;
import com.bachhoa.workmanagement.user.dto.ChangeStatusRequest;
import com.bachhoa.workmanagement.user.dto.CreateUserRequest;
import com.bachhoa.workmanagement.user.dto.AssignableUserResponse;
import com.bachhoa.workmanagement.user.dto.UpdateMyProfileRequest;
import com.bachhoa.workmanagement.user.dto.UpdateUserRequest;
import com.bachhoa.workmanagement.user.dto.UserResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final String DEFAULT_ROLE = "MEMBER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(findRole(request.roleCode()));

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = findUser(id);
        String username = request.username().trim();

        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new ConflictException("Username đã tồn tại");
        }

        user.setUsername(username);
        user.setFullName(request.fullName().trim());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeRole(UUID id, ChangeRoleRequest request) {
        User user = findUser(id);
        user.setRole(findRole(request.roleCode()));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse changeStatus(UUID id, ChangeStatusRequest request) {
        User user = findUser(id);
        user.setStatus(request.status());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateMyProfile(String username, UpdateMyProfileRequest request) {
        User user = userRepository.findByUsernameWithRole(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng đang đăng nhập"));
        user.setFullName(request.fullName().trim());
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AssignableUserResponse> getAssignableUsers() {
        return userRepository.findAssignableUsers(UserStatus.ACTIVE, DEFAULT_ROLE).stream()
                .map(user -> new AssignableUserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getFullName(),
                        user.getRole().getCode()))
                .toList();
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + id));
    }

    private Role findRole(String roleCode) {
        String normalizedCode = roleCode == null || roleCode.isBlank()
                ? DEFAULT_ROLE
                : roleCode.trim().toUpperCase(Locale.ROOT);

        return roleRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy vai trò: " + normalizedCode));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getStatus(),
                user.getRole().getCode(),
                user.getRole().getName(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
