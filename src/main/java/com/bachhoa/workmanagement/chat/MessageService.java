package com.bachhoa.workmanagement.chat;

import com.bachhoa.workmanagement.chat.dto.MessageResponse;
import com.bachhoa.workmanagement.chat.dto.SendMessageRequest;
import com.bachhoa.workmanagement.chat.dto.UpdateMessageRequest;
import com.bachhoa.workmanagement.common.exception.ResourceNotFoundException;
import com.bachhoa.workmanagement.entity.ChatRoom;
import com.bachhoa.workmanagement.entity.Message;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.repository.ChatRoomMemberRepository;
import com.bachhoa.workmanagement.repository.ChatRoomRepository;
import com.bachhoa.workmanagement.repository.MessageRepository;
import com.bachhoa.workmanagement.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(
            UUID roomId,
            Authentication authentication) {
        User actor = currentUser(authentication);
        requireMember(roomId, actor);
        return messageRepository.findByRoom_IdAndDeletedAtIsNullOrderByCreatedAtAsc(roomId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MessageResponse send(
            UUID roomId,
            SendMessageRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        requireMember(roomId, actor);

        Message message = new Message();
        message.setRoom(room);
        message.setSender(actor);
        message.setContent(request.content().trim());
        return toResponse(messageRepository.save(message));
    }

    @Transactional
    public MessageResponse update(
            UUID roomId,
            UUID messageId,
            UpdateMessageRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        requireMember(roomId, actor);
        Message message = findMessage(roomId, messageId);
        assertSender(message, actor);

        message.setContent(request.content().trim());
        return toResponse(messageRepository.save(message));
    }

    @Transactional
    public void delete(
            UUID roomId,
            UUID messageId,
            Authentication authentication) {
        User actor = currentUser(authentication);
        requireMember(roomId, actor);
        Message message = findMessage(roomId, messageId);
        assertSender(message, actor);

        message.setDeletedAt(LocalDateTime.now());
        messageRepository.save(message);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameWithRole(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng đang đăng nhập"));
    }

    private ChatRoom findRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng chat: " + roomId));
    }

    private Message findMessage(UUID roomId, UUID messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy tin nhắn: " + messageId));
        if (!message.getRoom().getId().equals(roomId) || message.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Không tìm thấy tin nhắn trong phòng chat");
        }
        return message;
    }

    private void requireMember(UUID roomId, User actor) {
        findRoom(roomId);
        if (!chatRoomMemberRepository.existsByRoom_IdAndUser_Id(roomId, actor.getId())) {
            throw new AccessDeniedException("Bạn không phải thành viên của phòng chat");
        }
    }

    private void assertSender(Message message, User actor) {
        if (!message.getSender().getId().equals(actor.getId())) {
            throw new AccessDeniedException(
                    "Chỉ người gửi mới được sửa hoặc xóa tin nhắn");
        }
    }

    private MessageResponse toResponse(Message message) {
        User sender = message.getSender();
        return new MessageResponse(
                message.getId(),
                message.getRoom().getId(),
                sender.getId(),
                sender.getUsername(),
                message.getContent(),
                message.getCreatedAt(),
                message.getUpdatedAt());
    }
}
