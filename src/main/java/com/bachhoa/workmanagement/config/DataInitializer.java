package com.bachhoa.workmanagement.config;

import com.bachhoa.workmanagement.entity.Role;
import com.bachhoa.workmanagement.entity.User;
import com.bachhoa.workmanagement.entity.enums.UserStatus;
import com.bachhoa.workmanagement.repository.RoleRepository;
import com.bachhoa.workmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor 
public class DataInitializer {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed.enabled:false}")
	private boolean seedEnabled;

	@Value("${app.seed.admin-username:admin}")
	private String adminUsername;

	@Value("${app.seed.admin-password:admin123}")
	private String adminPassword;

	@Bean
	CommandLineRunner seedData() {
		return args -> {
			if (!seedEnabled) {
				return;
			}

			Role adminRole = getOrCreateRole("ADMIN", "Quản trị viên");
			getOrCreateRole("MANAGER", "Quản lý");
			getOrCreateRole("MEMBER", "Thành viên");

			if (userRepository.findByUsername(adminUsername).isEmpty()) {
				User admin = new User();
				admin.setUsername(adminUsername);
				admin.setPassword(passwordEncoder.encode(adminPassword));
				admin.setFullName("System Administrator");
				admin.setStatus(UserStatus.ACTIVE);
				admin.setRole(adminRole);
				userRepository.save(admin);
			}
		};
	}

	private Role getOrCreateRole(String code, String name) {
		return roleRepository.findByCode(code).orElseGet(() -> {
			Role role = new Role();
			role.setCode(code);
			role.setName(name);
			return roleRepository.save(role);
		});
	}
}
