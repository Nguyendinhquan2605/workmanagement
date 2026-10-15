package com.bachhoa.workmanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final String secret;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.secret = secret;
		this.expirationMs = expirationMs;
	}

	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.subject(userDetails.getUsername())
				.claim("role", userDetails.getAuthorities().stream()
						.findFirst()
						.map(authority -> authority.getAuthority().replace("ROLE_", ""))
						.orElse(null))
				.issuedAt(now)
				.expiration(expiration)
				.signWith(getSigningKey(), Jwts.SIG.HS256)
				.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		try {
			Claims claims = extractAllClaims(token);
			return claims.getSubject().equals(userDetails.getUsername())
					&& claims.getExpiration().after(new Date())
					&& userDetails.isEnabled();
		} catch (JwtException | IllegalArgumentException exception) {
			return false;
		}
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}
}
