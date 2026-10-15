package com.bachhoa.workmanagement.auth;

import com.bachhoa.workmanagement.auth.dto.LoginRequest;
import com.bachhoa.workmanagement.auth.dto.LoginResponse;
import com.bachhoa.workmanagement.entity.security.CustomUserPrincipal;
import com.bachhoa.workmanagement.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	@Value("${app.jwt.expiration-ms}")
	private long expirationMs;

	public LoginResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.username(), request.password()));

		CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
		String token = jwtService.generateToken(principal);
		String role = principal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

		return new LoginResponse(token, "Bearer", expirationMs, principal.getUsername(), role);
	}
}
