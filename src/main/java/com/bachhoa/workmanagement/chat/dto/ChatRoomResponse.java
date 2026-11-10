package com.bachhoa.workmanagement.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatRoomResponse(
        UUID id,
        String name,
        String description,
        UUID createdById,
        String createdByUsername,
        long memberCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
