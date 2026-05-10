package com.example.ticketsystem.controller;

import com.example.ticketsystem.services.AuthenticatedUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SecurityModelAdvice {

	private final AuthenticatedUserService authenticatedUserService;

	public SecurityModelAdvice(AuthenticatedUserService authenticatedUserService) {
		this.authenticatedUserService = authenticatedUserService;
	}

	@ModelAttribute("currentUsername")
	public String currentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
			return null;
		}
		return authentication.getName();
	}

	@ModelAttribute("currentPath")
	public String currentPath(HttpServletRequest request) {
		return request.getRequestURI();
	}

	@ModelAttribute("isSupport")
	public boolean isSupport() {
		// Werte fuer die Anzeige in Thymeleaf.
		return authenticatedUserService.hasRole("SUPPORT");
	}

	@ModelAttribute("isAdmin")
	public boolean isAdmin() {
		return authenticatedUserService.hasRole("ADMIN");
	}
}
