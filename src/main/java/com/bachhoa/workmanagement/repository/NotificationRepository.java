package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
	List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);

	List<Notification> findByUser_IdAndReadFalseOrderByCreatedAtDesc(UUID userId);
}
