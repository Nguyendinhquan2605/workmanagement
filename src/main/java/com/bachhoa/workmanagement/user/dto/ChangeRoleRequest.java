package com.bachhoa.workmanagement.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeRoleRequest(
        @NotBlank(message = "Mã vai trò không được để trống")
        @Size(max = 50, message = "Mã vai trò không được vượt quá 50 ký tự")
        String roleCode) {
}
