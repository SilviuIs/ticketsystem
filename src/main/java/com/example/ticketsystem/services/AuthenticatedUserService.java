package com.example.ticketsystem.services;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.repository.AppUserRepository;
import java.util.Collection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

	private final AppUserRepository userRepository;

	public AuthenticatedUserService(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public AppUser currentUser() {
		String username = currentUsername();
		return userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + username));
	}

	public String currentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new AccessDeniedException("No authenticated user available");
		}
		return authentication.getName();
	}

	public boolean hasRole(String role) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && hasRole(authentication.getAuthorities(), role);
	}

	public boolean isSupportOrAdmin() {
		return hasRole("SUPPORT") || hasRole("ADMIN");
	}

	private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
		String authorityName = "ROLE_" + role;
		return authorities.stream().anyMatch(authority -> authority.getAuthority().equals(authorityName));
	}
}
