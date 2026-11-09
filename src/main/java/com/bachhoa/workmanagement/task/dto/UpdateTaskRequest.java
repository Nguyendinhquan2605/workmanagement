package com.bachhoa.workmanagement.task.dto;

import com.bachhoa.workmanagement.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateTaskRequest(
        @NotBlank(message = "Tiêu đề công việc không được để trống")
        @Size(max = 200, message = "Tiêu đề công việc không được vượt quá 200 ký tự")
        String title,

        @Size(max = 5000, message = "Mô tả không được vượt quá 5000 ký tự")
        String description,

        TaskPriority priority,
        LocalDate startDate,
        LocalDate dueDate) {
}
