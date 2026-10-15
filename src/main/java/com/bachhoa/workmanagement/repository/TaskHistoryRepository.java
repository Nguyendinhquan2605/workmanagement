package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.TaskHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, UUID> {
	List<TaskHistory> findByTask_IdOrderByChangedAtAsc(UUID taskId);
}
