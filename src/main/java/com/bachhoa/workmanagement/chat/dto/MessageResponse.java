package com.bachhoa.workmanagement.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID roomId,
        UUID senderId,
        String senderUsername,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
