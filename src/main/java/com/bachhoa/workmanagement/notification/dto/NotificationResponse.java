package com.bachhoa.workmanagement.notification.dto;

import com.bachhoa.workmanagement.entity.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        String content,
        boolean read,
        UUID taskId,
        UUID roomId,
        LocalDateTime createdAt) {
}
