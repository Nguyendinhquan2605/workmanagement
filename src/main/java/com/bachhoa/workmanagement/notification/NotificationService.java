package com.bachhoa.workmanagement.notification;

import com.bachhoa.workmanagement.common.exception.ResourceNotFoundException;
import com.bachhoa.workmanagement.entity.ChatRoom;
import com.bachhoa.workmanagement.entity.Notification;
import com.bachhoa.workmanagement.entity.Task;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.NotificationType;
import com.bachhoa.workmanagement.notification.dto.NotificationResponse;
import com.bachhoa.workmanagement.repository.NotificationRepository;
import com.bachhoa.workmanagement.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(
            Authentication authentication,
            Boolean isRead) {
        User recipient = currentUser(authentication);
        List<Notification> notifications;
        if (Boolean.FALSE.equals(isRead)) {
            notifications = notificationRepository
                    .findByUser_IdAndReadFalseOrderByCreatedAtDesc(recipient.getId());
        } else if (Boolean.TRUE.equals(isRead)) {
            notifications = notificationRepository
                    .findByUser_IdAndReadTrueOrderByCreatedAtDesc(recipient.getId());
        } else {
            notifications = notificationRepository
                    .findByUser_IdOrderByCreatedAtDesc(recipient.getId());
        }
        return notifications.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id, Authentication authentication) {
        User recipient = currentUser(authentication);
        Notification notification = findNotification(id);
        assertRecipient(notification, recipient);
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse markAsRead(UUID id, Authentication authentication) {
        User recipient = currentUser(authentication);
        Notification notification = findNotification(id);
        assertRecipient(notification, recipient);
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllAsRead(Authentication authentication) {
        User recipient = currentUser(authentication);
        List<Notification> unread = notificationRepository
                .findByUser_IdAndReadFalseOrderByCreatedAtDesc(recipient.getId());
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    @Transactional
    public void notifyTaskAssigned(Task task) {
        if (task.getAssignedTo() == null) {
            return;
        }
        create(
                task.getAssignedTo(),
                task,
                null,
                NotificationType.TASK_ASSIGNED,
                "Bạn được giao công việc: " + task.getTitle());
    }

    @Transactional
    public void notifyTaskStatusChanged(Task task, User actor) {
        Map<UUID, User> recipients = new LinkedHashMap<>();
        if (task.getCreatedBy() != null) {
            recipients.put(task.getCreatedBy().getId(), task.getCreatedBy());
        }
        if (task.getAssignedTo() != null) {
            recipients.put(task.getAssignedTo().getId(), task.getAssignedTo());
        }
        recipients.remove(actor.getId());

        for (User recipient : recipients.values()) {
            create(
                    recipient,
                    task,
                    null,
                    NotificationType.TASK_STATUS_CHANGED,
                    "Trạng thái công việc '" + task.getTitle()
                            + "' đã được cập nhật thành " + task.getStatus());
        }
    }

    @Transactional
    public void notifyRoomMemberAdded(ChatRoom room, User recipient) {
        create(
                recipient,
                null,
                room,
                NotificationType.ROOM_MEMBER_ADDED,
                "Bạn đã được thêm vào phòng chat: " + room.getName());
    }

    @Transactional
    public void deleteByRoom(UUID roomId) {
        notificationRepository.deleteByRoom_Id(roomId);
    }

    private Notification create(
            User recipient,
            Task task,
            ChatRoom room,
            NotificationType type,
            String content) {
        Notification notification = new Notification();
        notification.setUser(recipient);
        notification.setTask(task);
        notification.setRoom(room);
        notification.setType(type);
        notification.setContent(content);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameWithRole(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng đang đăng nhập"));
    }

    private Notification findNotification(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông báo: " + id));
    }

    private void assertRecipient(Notification notification, User recipient) {
        if (notification.getUser() == null
                || !notification.getUser().getId().equals(recipient.getId())) {
            throw new AccessDeniedException("Bạn không có quyền truy cập thông báo này");
        }
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getContent(),
                notification.isRead(),
                notification.getTask() == null ? null : notification.getTask().getId(),
                notification.getRoom() == null ? null : notification.getRoom().getId(),
                notification.getCreatedAt());
    }
}
