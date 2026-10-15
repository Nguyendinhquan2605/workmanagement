package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.Task;
import com.bachhoa.workmanagement.entity.enums.TaskStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
	List<Task> findByAssignedTo_Id(UUID userId);

	List<Task> findByStatus(TaskStatus status);
}
