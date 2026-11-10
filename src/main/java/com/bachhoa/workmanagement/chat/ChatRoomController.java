package com.bachhoa.workmanagement.chat;

import com.bachhoa.workmanagement.chat.dto.AddChatRoomMemberRequest;
import com.bachhoa.workmanagement.chat.dto.ChatRoomMemberResponse;
import com.bachhoa.workmanagement.chat.dto.ChatRoomResponse;
import com.bachhoa.workmanagement.chat.dto.CreateChatRoomRequest;
import com.bachhoa.workmanagement.chat.dto.UpdateChatRoomRequest;
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
@RequestMapping("/api/chat-rooms")
@Tag(name = "Chat Room Management", description = "Quản lý phòng chat và thành viên")
@SecurityRequirement(name = "bearerAuth")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    public ChatRoomController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo phòng chat")
    public ResponseEntity<ChatRoomResponse> create(
            @Valid @RequestBody CreateChatRoomRequest request,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        ChatRoomResponse response = chatRoomService.create(request, authentication);
        URI location = uriBuilder.path("/api/chat-rooms/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sách phòng chat được phép truy cập")
    public List<ChatRoomResponse> getRooms(Authentication authentication) {
        return chatRoomService.getRooms(authentication);
    }

    @GetMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem thông tin phòng chat")
    public ChatRoomResponse getById(
            @PathVariable UUID roomId,
            Authentication authentication) {
        return chatRoomService.getById(roomId, authentication);
    }

    @PutMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật phòng chat")
    public ChatRoomResponse update(
            @PathVariable UUID roomId,
            @Valid @RequestBody UpdateChatRoomRequest request,
            Authentication authentication) {
        return chatRoomService.update(roomId, request, authentication);
    }

    @DeleteMapping("/{roomId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa phòng chat")
    public ResponseEntity<Void> delete(
            @PathVariable UUID roomId,
            Authentication authentication) {
        chatRoomService.delete(roomId, authentication);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem thành viên phòng chat")
    public List<ChatRoomMemberResponse> getMembers(
            @PathVariable UUID roomId,
            Authentication authentication) {
        return chatRoomService.getMembers(roomId, authentication);
    }

    @PostMapping("/{roomId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Thêm thành viên vào phòng chat")
    public ResponseEntity<ChatRoomMemberResponse> addMember(
            @PathVariable UUID roomId,
            @Valid @RequestBody AddChatRoomMemberRequest request,
            Authentication authentication) {
        ChatRoomMemberResponse response = chatRoomService.addMember(
                roomId,
                request,
                authentication);
        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{roomId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa thành viên khỏi phòng chat")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID roomId,
            @PathVariable UUID userId,
            Authentication authentication) {
        chatRoomService.removeMember(roomId, userId, authentication);
        return ResponseEntity.noContent().build();
    }
}
