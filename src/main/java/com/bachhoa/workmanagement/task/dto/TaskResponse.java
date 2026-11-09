package com.bachhoa.workmanagement.task.dto;

import com.bachhoa.workmanagement.entity.enums.TaskPriority;
import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDate startDate,
        LocalDate dueDate,
        UUID createdById,
        String createdByUsername,
        UUID assignedToId,
        String assignedToUsername,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
