package com.bachhoa.workmanagement.user.dto;

import com.bachhoa.workmanagement.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeStatusRequest(
        @NotNull(message = "Trạng thái không được để trống")
        UserStatus status) {
}
