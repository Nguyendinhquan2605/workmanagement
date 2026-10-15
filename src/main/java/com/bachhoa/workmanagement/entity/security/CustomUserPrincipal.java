package com.bachhoa.workmanagement.entity.security;

import com.bachhoa.workmanagement.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserPrincipal implements UserDetails {

	private final UUID id;
	private final String username;
	private final String password;
	private final String role;
	private final boolean enabled;

	private CustomUserPrincipal(UUID id, String username, String password, String role, boolean enabled) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.enabled = enabled;
	}

	public static CustomUserPrincipal from(User user) {
		return new CustomUserPrincipal(
				user.getId(),
				user.getUsername(),
				user.getPassword(),
				user.getRole().getCode(),
				user.getStatus().name().equals("ACTIVE"));
	}

	public UUID getId() {
		return id;
	}

	public String getRole() {
		return role;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
