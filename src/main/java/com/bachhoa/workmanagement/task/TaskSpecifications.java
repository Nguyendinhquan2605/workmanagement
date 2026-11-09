package com.bachhoa.workmanagement.task;

import com.bachhoa.workmanagement.entity.Task;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.TaskPriority;
import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> visibleTo(User user) {
        String role = user.getRole().getCode();
        UUID userId = user.getId();

        if ("ADMIN".equals(role)) {
            return unrestricted();
        }

        if ("MANAGER".equals(role)) {
            return (root, query, builder) -> builder.or(
                    builder.equal(root.get("createdBy").get("id"), userId),
                    builder.equal(root.get("assignedTo").get("id"), userId));
        }

        return (root, query, builder) -> builder.equal(
                root.get("assignedTo").get("id"), userId);
    }

    public static Specification<Task> createdBy(UUID userId) {
        return (root, query, builder) -> builder.equal(
                root.get("createdBy").get("id"), userId);
    }

    public static Specification<Task> hasStatus(TaskStatus status) {
        return status == null ? unrestricted()
                : (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return priority == null ? unrestricted()
                : (root, query, builder) -> builder.equal(root.get("priority"), priority);
    }

    public static Specification<Task> assignedTo(UUID userId) {
        return userId == null ? unrestricted()
                : (root, query, builder) -> builder.equal(
                        root.get("assignedTo").get("id"), userId);
    }

    public static Specification<Task> keyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return unrestricted();
        }

        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("title")), pattern),
                builder.like(builder.lower(root.get("description")), pattern));
    }

    public static Specification<Task> dueDateFrom(LocalDate dueDateFrom) {
        return dueDateFrom == null ? unrestricted()
                : (root, query, builder) -> builder.greaterThanOrEqualTo(
                        root.get("dueDate"), dueDateFrom);
    }

    public static Specification<Task> dueDateTo(LocalDate dueDateTo) {
        return dueDateTo == null ? unrestricted()
                : (root, query, builder) -> builder.lessThanOrEqualTo(
                        root.get("dueDate"), dueDateTo);
    }

    public static Specification<Task> overdue(Boolean overdue) {
        if (!Boolean.TRUE.equals(overdue)) {
            return unrestricted();
        }

        return (root, query, builder) -> builder.and(
                builder.isNotNull(root.get("dueDate")),
                builder.lessThan(root.get("dueDate"), LocalDate.now()),
                builder.notEqual(root.get("status"), TaskStatus.COMPLETED),
                builder.notEqual(root.get("status"), TaskStatus.CANCELLED));
    }

    private static Specification<Task> unrestricted() {
        return (root, query, builder) -> builder.conjunction();
    }
}
