package com.bachhoa.workmanagement.repository;

import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {
	@Query("select u from User u join fetch u.role where u.username = :username")
	Optional<User> findByUsernameWithRole(@Param("username") String username);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, UUID id);

	@Query("select u from User u join fetch u.role "
			+ "where u.status = :status and u.role.code = :roleCode "
			+ "order by u.fullName asc")
	List<User> findAssignableUsers(
			@Param("status") UserStatus status,
			@Param("roleCode") String roleCode);
}
