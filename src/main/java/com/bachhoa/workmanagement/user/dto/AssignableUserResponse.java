package com.bachhoa.workmanagement.user.dto;

import java.util.UUID;

public record AssignableUserResponse(
        UUID id,
        String username,
        String fullName,
        String roleCode) {
}
