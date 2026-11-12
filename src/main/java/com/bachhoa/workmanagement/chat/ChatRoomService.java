package com.bachhoa.workmanagement.chat;

import com.bachhoa.workmanagement.chat.dto.AddChatRoomMemberRequest;
import com.bachhoa.workmanagement.chat.dto.ChatRoomMemberResponse;
import com.bachhoa.workmanagement.chat.dto.ChatRoomResponse;
import com.bachhoa.workmanagement.chat.dto.CreateChatRoomRequest;
import com.bachhoa.workmanagement.chat.dto.UpdateChatRoomRequest;
import com.bachhoa.workmanagement.common.exception.BadRequestException;
import com.bachhoa.workmanagement.common.exception.ConflictException;
import com.bachhoa.workmanagement.common.exception.ResourceNotFoundException;
import com.bachhoa.workmanagement.entity.ChatRoom;
import com.bachhoa.workmanagement.entity.ChatRoomMember;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.UserStatus;
import com.bachhoa.workmanagement.notification.NotificationService;
import com.bachhoa.workmanagement.repository.ChatRoomMemberRepository;
import com.bachhoa.workmanagement.repository.ChatRoomRepository;
import com.bachhoa.workmanagement.repository.MessageRepository;
import com.bachhoa.workmanagement.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ChatRoomService(
            ChatRoomRepository chatRoomRepository,
            ChatRoomMemberRepository chatRoomMemberRepository,
            MessageRepository messageRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ChatRoomResponse create(
            CreateChatRoomRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        assertCanCreateRoom(actor);

        ChatRoom room = new ChatRoom();
        room.setName(request.name().trim());
        room.setDescription(request.description());
        room.setCreatedBy(actor);
        room = chatRoomRepository.save(room);

        addMembership(room, actor);
        return toRoomResponse(room);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(Authentication authentication) {
        User actor = currentUser(authentication);

        if (isAdmin(actor)) {
            return chatRoomRepository.findAll().stream()
                    .map(this::toRoomResponse)
                    .toList();
        }

        return chatRoomMemberRepository.findByUser_Id(actor.getId()).stream()
                .map(ChatRoomMember::getRoom)
                .map(this::toRoomResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomResponse getById(UUID roomId, Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        assertCanViewRoom(room, actor);
        return toRoomResponse(room);
    }

    @Transactional
    public ChatRoomResponse update(
            UUID roomId,
            UpdateChatRoomRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        assertCanManageRoom(room, actor);

        room.setName(request.name().trim());
        room.setDescription(request.description());
        return toRoomResponse(chatRoomRepository.save(room));
    }

    @Transactional
    public void delete(UUID roomId, Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        assertCanManageRoom(room, actor);

        notificationService.deleteByRoom(roomId);
        messageRepository.deleteByRoom_Id(roomId);
        chatRoomMemberRepository.deleteByRoom_Id(roomId);
        chatRoomRepository.delete(room);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomMemberResponse> getMembers(
            UUID roomId,
            Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        assertCanViewRoom(room, actor);

        return chatRoomMemberRepository.findByRoom_IdOrderByJoinedAtAsc(roomId).stream()
                .map(this::toMemberResponse)
                .toList();
    }

    @Transactional
    public ChatRoomMemberResponse addMember(
            UUID roomId,
            AddChatRoomMemberRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        assertCanManageRoom(room, actor);

        if (chatRoomMemberRepository.existsByRoom_IdAndUser_Id(roomId, request.userId())) {
            throw new ConflictException("Người dùng đã là thành viên của phòng chat");
        }

        User user = findActiveUser(request.userId());
        ChatRoomMember membership = addMembership(room, user);
        notificationService.notifyRoomMemberAdded(room, user);
        return toMemberResponse(membership);
    }

    @Transactional
    public void removeMember(
            UUID roomId,
            UUID userId,
            Authentication authentication) {
        User actor = currentUser(authentication);
        ChatRoom room = findRoom(roomId);
        assertCanManageRoom(room, actor);

        if (room.getCreatedBy().getId().equals(userId)) {
            throw new BadRequestException("Không được xóa người tạo phòng khỏi phòng chat");
        }

        ChatRoomMember membership = chatRoomMemberRepository
                .findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Người dùng không phải thành viên của phòng chat"));
        chatRoomMemberRepository.delete(membership);
    }

    private ChatRoomMember addMembership(ChatRoom room, User user) {
        ChatRoomMember membership = new ChatRoomMember();
        membership.setRoom(room);
        membership.setUser(user);
        return chatRoomMemberRepository.save(membership);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameWithRole(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng đang đăng nhập"));
    }

    private User findActiveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng: " + userId));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Chỉ được thêm người dùng đang ACTIVE");
        }
        return user;
    }

    private ChatRoom findRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phòng chat: " + roomId));
    }

    private void assertCanCreateRoom(User actor) {
        if (!isAdmin(actor) && !isManager(actor)) {
            throw new AccessDeniedException("Chỉ ADMIN hoặc MANAGER được tạo phòng chat");
        }
    }

    private void assertCanViewRoom(ChatRoom room, User actor) {
        if (isAdmin(actor)) {
            return;
        }

        if (!chatRoomMemberRepository.existsByRoom_IdAndUser_Id(room.getId(), actor.getId())) {
            throw new AccessDeniedException("Bạn không phải thành viên của phòng chat");
        }
    }

    private void assertCanManageRoom(ChatRoom room, User actor) {
        if (isAdmin(actor)) {
            return;
        }

        if (isManager(actor)
                && room.getCreatedBy().getId().equals(actor.getId())) {
            return;
        }

        throw new AccessDeniedException(
                "Chỉ ADMIN hoặc người tạo phòng mới được quản lý phòng chat");
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room) {
        User creator = room.getCreatedBy();
        return new ChatRoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                creator.getId(),
                creator.getUsername(),
                chatRoomMemberRepository.countByRoom_Id(room.getId()),
                room.getCreatedAt(),
                room.getUpdatedAt());
    }

    private ChatRoomMemberResponse toMemberResponse(ChatRoomMember membership) {
        User user = membership.getUser();
        return new ChatRoomMemberResponse(
                membership.getId(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole().getCode(),
                user.getStatus(),
                membership.getJoinedAt());
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole().getCode());
    }

    private boolean isManager(User user) {
        return "MANAGER".equals(user.getRole().getCode());
    }
}
