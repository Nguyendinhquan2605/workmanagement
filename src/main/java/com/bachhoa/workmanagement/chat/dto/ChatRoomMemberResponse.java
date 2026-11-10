package com.bachhoa.workmanagement.chat.dto;

import com.bachhoa.workmanagement.entity.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomMemberResponse(
        UUID id,
        UUID userId,
        String username,
        String fullName,
        String roleCode,
        UserStatus status,
        LocalDateTime joinedAt) {
}
