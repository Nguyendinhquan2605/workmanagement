package com.bachhoa.workmanagement.task.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignTaskRequest(
        @NotNull(message = "Người phụ trách không được để trống")
        UUID assignedToId) {
}
