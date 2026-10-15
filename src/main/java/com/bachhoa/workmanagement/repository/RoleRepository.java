package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
	Optional<Role> findByCode(String code);
}
