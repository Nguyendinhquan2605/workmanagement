package com.bachhoa.workmanagement.task.dto;

import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeTaskStatusRequest(
        @NotNull(message = "Trạng thái không được để trống")
        TaskStatus status) {
}
