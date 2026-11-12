package com.bachhoa.workmanagement.notification;

import com.bachhoa.workmanagement.notification.dto.NotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Management", description = "Quản lý thông báo cá nhân")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem danh sách thông báo, có thể lọc theo trạng thái đã đọc")
    public List<NotificationResponse> getNotifications(
            Authentication authentication,
            @RequestParam(required = false) Boolean isRead) {
        return notificationService.getNotifications(authentication, isRead);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem chi tiết một thông báo")
    public NotificationResponse getById(
            @PathVariable UUID id,
            Authentication authentication) {
        return notificationService.getById(id, authentication);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu một thông báo đã đọc")
    public NotificationResponse markAsRead(
            @PathVariable UUID id,
            Authentication authentication) {
        return notificationService.markAsRead(id, authentication);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Đánh dấu tất cả thông báo đã đọc")
    public Map<String, Integer> markAllAsRead(Authentication authentication) {
        return Map.of("markedCount", notificationService.markAllAsRead(authentication));
    }
}
