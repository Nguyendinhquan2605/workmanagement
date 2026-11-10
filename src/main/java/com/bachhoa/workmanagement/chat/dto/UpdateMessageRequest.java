package com.bachhoa.workmanagement.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateMessageRequest(
        @NotBlank(message = "Nội dung tin nhắn không được để trống")
        @Size(max = 5000, message = "Nội dung tin nhắn không được vượt quá 5000 ký tự")
        String content) {
}
