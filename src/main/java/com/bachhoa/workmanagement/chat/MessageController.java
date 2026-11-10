package com.bachhoa.workmanagement.chat;

import com.bachhoa.workmanagement.chat.dto.MessageResponse;
import com.bachhoa.workmanagement.chat.dto.SendMessageRequest;
import com.bachhoa.workmanagement.chat.dto.UpdateMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/chat-rooms/{roomId}/messages")
@Tag(name = "Message Management", description = "Gửi và quản lý tin nhắn phòng chat")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem tin nhắn trong phòng chat")
    public List<MessageResponse> getMessages(
            @PathVariable UUID roomId,
            Authentication authentication) {
        return messageService.getMessages(roomId, authentication);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Gửi tin nhắn")
    public ResponseEntity<MessageResponse> send(
            @PathVariable UUID roomId,
            @Valid @RequestBody SendMessageRequest request,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        MessageResponse response = messageService.send(roomId, request, authentication);
        URI location = uriBuilder.path(
                        "/api/chat-rooms/{roomId}/messages/{messageId}")
                .buildAndExpand(response.roomId(), response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Sửa tin nhắn của mình")
    public MessageResponse update(
            @PathVariable UUID roomId,
            @PathVariable UUID messageId,
            @Valid @RequestBody UpdateMessageRequest request,
            Authentication authentication) {
        return messageService.update(roomId, messageId, request, authentication);
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa mềm tin nhắn của mình")
    public ResponseEntity<Void> delete(
            @PathVariable UUID roomId,
            @PathVariable UUID messageId,
            Authentication authentication) {
        messageService.delete(roomId, messageId, authentication);
        return ResponseEntity.noContent().build();
    }
}
