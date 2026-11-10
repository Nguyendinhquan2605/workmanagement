package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.ChatRoom;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {
	List<ChatRoom> findByCreatedBy_IdOrderByCreatedAtDesc(UUID userId);
}
