package com.bachhoa.workmanagement.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Username không được để trống")
        @Size(max = 50, message = "Username không được vượt quá 50 ký tự")
        String username,

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
        String fullName) {
}
