package com.bachhoa.workmanagement.auth;

import com.bachhoa.workmanagement.auth.dto.LoginRequest;
import com.bachhoa.workmanagement.auth.dto.LoginResponse;
import com.bachhoa.workmanagement.auth.dto.MeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor 
@Tag(name = "Authentication", description = "Đăng nhập và xác thực người dùng")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	@Operation(summary = "Đăng nhập", security = {})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	@Operation(summary = "Lấy thông tin người dùng đang đăng nhập")
	@SecurityRequirement(name = "bearerAuth")
	public MeResponse me(Authentication authentication) {
		return new MeResponse(
				authentication.getName(),
				authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", ""));
	}

	@GetMapping("/member-only")
	@PreAuthorize("hasRole('MEMBER')")
	@Operation(summary = "Endpoint kiểm tra quyền MEMBER")
	@SecurityRequirement(name = "bearerAuth")
	public String memberOnly() {
		return "Bạn có quyền MEMBER";
	}
}
