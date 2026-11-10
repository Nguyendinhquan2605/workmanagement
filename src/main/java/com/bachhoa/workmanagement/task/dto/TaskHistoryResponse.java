package com.bachhoa.workmanagement.task.dto;

import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskHistoryResponse(
        UUID id,
        UUID taskId,
        TaskStatus oldStatus,
        TaskStatus newStatus,
        UUID changedById,
        String changedByUsername,
        LocalDateTime changedAt) {
}
