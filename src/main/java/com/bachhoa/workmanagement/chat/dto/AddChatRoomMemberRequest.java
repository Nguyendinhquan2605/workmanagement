package com.bachhoa.workmanagement.chat.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddChatRoomMemberRequest(
        @NotNull(message = "userId không được để trống")
        UUID userId) {
}
