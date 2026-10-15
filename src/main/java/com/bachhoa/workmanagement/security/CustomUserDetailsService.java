package com.bachhoa.workmanagement.security;

import com.bachhoa.workmanagement.entity.security.CustomUserPrincipal;
import com.bachhoa.workmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsernameWithRole(username)
				.map(CustomUserPrincipal::from)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
