package com.bachhoa.workmanagement.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateChatRoomRequest(
        @NotBlank(message = "Tên phòng chat không được để trống")
        @Size(max = 100, message = "Tên phòng chat không được vượt quá 100 ký tự")
        String name,

        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
        String description) {
}
