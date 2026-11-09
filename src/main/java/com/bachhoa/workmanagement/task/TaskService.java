package com.bachhoa.workmanagement.task;

import com.bachhoa.workmanagement.common.exception.BadRequestException;
import com.bachhoa.workmanagement.common.exception.ResourceNotFoundException;
import com.bachhoa.workmanagement.entity.Task;
import com.bachhoa.workmanagement.entity.TaskHistory;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.TaskPriority;
import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import com.bachhoa.workmanagement.entity.enums.UserStatus;
import com.bachhoa.workmanagement.repository.TaskHistoryRepository;
import com.bachhoa.workmanagement.repository.TaskRepository;
import com.bachhoa.workmanagement.repository.UserRepository;
import com.bachhoa.workmanagement.task.dto.AssignTaskRequest;
import com.bachhoa.workmanagement.task.dto.ChangeTaskStatusRequest;
import com.bachhoa.workmanagement.task.dto.CreateTaskRequest;
import com.bachhoa.workmanagement.task.dto.TaskResponse;
import com.bachhoa.workmanagement.task.dto.UpdateTaskRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            TaskHistoryRepository taskHistoryRepository,
            UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> search(
            Authentication authentication,
            TaskStatus status,
            TaskPriority priority,
            UUID assignedToId,
            String keyword,
            LocalDate dueDateFrom,
            LocalDate dueDateTo,
            Boolean overdue) {
        User actor = currentUser(authentication);
        Specification<Task> specification = TaskSpecifications.visibleTo(actor)
                .and(TaskSpecifications.hasStatus(status))
                .and(TaskSpecifications.hasPriority(priority))
                .and(TaskSpecifications.assignedTo(assignedToId))
                .and(TaskSpecifications.keyword(keyword))
                .and(TaskSpecifications.dueDateFrom(dueDateFrom))
                .and(TaskSpecifications.dueDateTo(dueDateTo))
                .and(TaskSpecifications.overdue(overdue));

        return taskRepository.findAll(specification,
                        Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(UUID id, Authentication authentication) {
        User actor = currentUser(authentication);
        Task task = findTask(id);
        assertCanView(task, actor);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse create(CreateTaskRequest request, Authentication authentication) {
        User actor = currentUser(authentication);
        assertCanCreate(actor);
        validateDates(request.startDate(), request.dueDate());

        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setPriority(request.priority() == null ? TaskPriority.MEDIUM : request.priority());
        task.setStatus(TaskStatus.TODO);
        task.setStartDate(request.startDate());
        task.setDueDate(request.dueDate());
        task.setCreatedBy(actor);
        task.setAssignedTo(resolveAssignee(request.assignedToId()));

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(
            UUID id,
            UpdateTaskRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        Task task = findTask(id);
        assertCanManage(task, actor);
        validateDates(request.startDate(), request.dueDate());

        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setPriority(request.priority() == null ? task.getPriority() : request.priority());
        task.setStartDate(request.startDate());
        task.setDueDate(request.dueDate());

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assign(
            UUID id,
            AssignTaskRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        Task task = findTask(id);
        assertCanManage(task, actor);

        task.setAssignedTo(resolveAssignee(request.assignedToId()));
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse changeStatus(
            UUID id,
            ChangeTaskStatusRequest request,
            Authentication authentication) {
        User actor = currentUser(authentication);
        Task task = findTask(id);
        assertCanChangeStatus(task, actor);

        TaskStatus oldStatus = task.getStatus();
        TaskStatus newStatus = request.status();
        validateStatusTransition(oldStatus, newStatus, actor);

        task.setStatus(newStatus);
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor);

        taskRepository.save(task);
        taskHistoryRepository.save(history);
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(Authentication authentication) {
        User actor = currentUser(authentication);
        return taskRepository.findByAssignedTo_IdOrderByDueDateAsc(actor.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getCreatedByMe(Authentication authentication) {
        User actor = currentUser(authentication);
        assertCanCreate(actor);
        return taskRepository.findByCreatedBy_IdOrderByCreatedAtDesc(actor.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsernameWithRole(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng đang đăng nhập"));
    }

    private Task findTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy công việc: " + id));
    }

    private User resolveAssignee(UUID assignedToId) {
        if (assignedToId == null) {
            return null;
        }

        User assignee = userRepository.findById(assignedToId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người được giao: " + assignedToId));

        if (assignee.getStatus() != UserStatus.ACTIVE
                || assignee.getRole() == null
                || !"MEMBER".equals(assignee.getRole().getCode())) {
            throw new BadRequestException(
                    "Người được giao phải là MEMBER đang ACTIVE");
        }
        return assignee;
    }

    private void assertCanCreate(User actor) {
        if (!isAdmin(actor) && !isManager(actor)) {
            throw new AccessDeniedException("Chỉ ADMIN hoặc MANAGER được tạo công việc");
        }
    }

    private void assertCanView(Task task, User actor) {
        if (isAdmin(actor)) {
            return;
        }

        if (isManager(actor)
                && (isCreatedBy(task, actor) || isAssignedTo(task, actor))) {
            return;
        }

        if (isMember(actor) && isAssignedTo(task, actor)) {
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền xem công việc này");
    }

    private void assertCanManage(Task task, User actor) {
        if (isAdmin(actor)) {
            return;
        }

        if (isManager(actor) && isCreatedBy(task, actor)) {
            return;
        }

        throw new AccessDeniedException(
                "Chỉ ADMIN hoặc MANAGER tạo công việc mới được quản lý công việc này");
    }

    private void assertCanChangeStatus(Task task, User actor) {
        if (isAdmin(actor)) {
            return;
        }

        if (isManager(actor) && isCreatedBy(task, actor)) {
            return;
        }

        if (isMember(actor) && isAssignedTo(task, actor)) {
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái công việc này");
    }

    private void validateStatusTransition(
            TaskStatus oldStatus,
            TaskStatus newStatus,
            User actor) {
        if (oldStatus == newStatus) {
            throw new BadRequestException("Trạng thái mới phải khác trạng thái hiện tại");
        }

        if (oldStatus == TaskStatus.CANCELLED) {
            throw new BadRequestException("CANCELLED là trạng thái kết thúc");
        }

        if (oldStatus == TaskStatus.TODO && newStatus == TaskStatus.COMPLETED) {
            throw new BadRequestException(
                    "Không được chuyển trực tiếp từ TODO sang COMPLETED");
        }

        if (isMember(actor)
                && !(oldStatus == TaskStatus.TODO && newStatus == TaskStatus.IN_PROGRESS)
                && !(oldStatus == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.COMPLETED)
                && !(oldStatus == TaskStatus.COMPLETED && newStatus == TaskStatus.IN_PROGRESS)) {
            throw new BadRequestException(
                    "MEMBER chỉ được chuyển TODO → IN_PROGRESS, IN_PROGRESS → COMPLETED "
                            + "hoặc COMPLETED → IN_PROGRESS");
        }

        if (newStatus == TaskStatus.CANCELLED && isMember(actor)) {
            throw new AccessDeniedException("MEMBER không được hủy công việc");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate dueDate) {
        if (startDate != null && dueDate != null && dueDate.isBefore(startDate)) {
            throw new BadRequestException("dueDate không được trước startDate");
        }
    }

    private TaskResponse toResponse(Task task) {
        User createdBy = task.getCreatedBy();
        User assignedTo = task.getAssignedTo();
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getStartDate(),
                task.getDueDate(),
                createdBy.getId(),
                createdBy.getUsername(),
                assignedTo == null ? null : assignedTo.getId(),
                assignedTo == null ? null : assignedTo.getUsername(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole().getCode());
    }

    private boolean isManager(User user) {
        return "MANAGER".equals(user.getRole().getCode());
    }

    private boolean isMember(User user) {
        return "MEMBER".equals(user.getRole().getCode());
    }

    private boolean isCreatedBy(Task task, User user) {
        return task.getCreatedBy() != null
                && task.getCreatedBy().getId().equals(user.getId());
    }

    private boolean isAssignedTo(Task task, User user) {
        return task.getAssignedTo() != null
                && task.getAssignedTo().getId().equals(user.getId());
    }
}
