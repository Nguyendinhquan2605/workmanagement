package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.Message;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {
	List<Message> findByRoom_IdAndDeletedAtIsNullOrderByCreatedAtAsc(UUID roomId);
}
