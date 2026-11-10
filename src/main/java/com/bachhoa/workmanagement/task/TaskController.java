package com.bachhoa.workmanagement.task;

import com.bachhoa.workmanagement.entity.enums.TaskPriority;
import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import com.bachhoa.workmanagement.task.dto.AssignTaskRequest;
import com.bachhoa.workmanagement.task.dto.ChangeTaskStatusRequest;
import com.bachhoa.workmanagement.task.dto.CreateTaskRequest;
import com.bachhoa.workmanagement.task.dto.TaskResponse;
import com.bachhoa.workmanagement.task.dto.TaskHistoryResponse;
import com.bachhoa.workmanagement.task.dto.UpdateTaskRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task Management", description = "Quản lý và theo dõi công việc")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Tạo công việc và có thể giao ngay")
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        TaskResponse response = taskService.create(request, authentication);
        URI location = uriBuilder.path("/api/tasks/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tìm kiếm và lọc công việc theo phạm vi người dùng")
    public List<TaskResponse> search(
            Authentication authentication,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) UUID assignedToId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate dueDateFrom,
            @RequestParam(required = false) LocalDate dueDateTo,
            @RequestParam(required = false) Boolean overdue) {
        return taskService.search(
                authentication,
                status,
                priority,
                assignedToId,
                keyword,
                dueDateFrom,
                dueDateTo,
                overdue);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem công việc được giao cho người dùng hiện tại")
    public List<TaskResponse> getMyTasks(Authentication authentication) {
        return taskService.getMyTasks(authentication);
    }

    @GetMapping("/created-by-me")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Xem công việc do người dùng hiện tại tạo")
    public List<TaskResponse> getCreatedByMe(Authentication authentication) {
        return taskService.getCreatedByMe(authentication);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem lịch sử thay đổi trạng thái công việc")
    public List<TaskHistoryResponse> getHistory(
            @PathVariable UUID id,
            Authentication authentication) {
        return taskService.getHistory(id, authentication);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xem chi tiết công việc")
    public TaskResponse getById(
            @PathVariable UUID id,
            Authentication authentication) {
        return taskService.getById(id, authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Cập nhật thông tin công việc")
    public TaskResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication authentication) {
        return taskService.update(id, request, authentication);
    }

    @PatchMapping("/{id}/assignee")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Thay đổi người phụ trách")
    public TaskResponse assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {
        return taskService.assign(id, request, authentication);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật trạng thái công việc")
    public TaskResponse changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeTaskStatusRequest request,
            Authentication authentication) {
        return taskService.changeStatus(id, request, authentication);
    }
}
