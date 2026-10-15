package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.ChatRoomMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, UUID> {
	Optional<ChatRoomMember> findByRoom_IdAndUser_Id(UUID roomId, UUID userId);

	boolean existsByRoom_IdAndUser_Id(UUID roomId, UUID userId);

	List<ChatRoomMember> findByRoom_Id(UUID roomId);
}
