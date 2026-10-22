package com.bachhoa.workmanagement.user.dto;

import com.bachhoa.workmanagement.entity.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String fullName,
        UserStatus status,
        String roleCode,
        String roleName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
