package com.example.ticketsystem.services;

import com.example.ticketsystem.models.ApiDtos.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitingFilter extends OncePerRequestFilter {

	private static final Duration WINDOW = Duration.ofMinutes(1);
	private static final String API_PREFIX = "api:";
	private static final String LOGIN_PREFIX = "login:";

	private final RateLimitService rateLimitService;
	private final ObjectMapper objectMapper;
	private final int apiRequestsPerMinute;
	private final int loginRequestsPerMinute;

	public RateLimitingFilter(
			RateLimitService rateLimitService,
			ObjectMapper objectMapper,
			int apiRequestsPerMinute,
			int loginRequestsPerMinute
	) {
		this.rateLimitService = rateLimitService;
		this.objectMapper = objectMapper;
		this.apiRequestsPerMinute = apiRequestsPerMinute;
		this.loginRequestsPerMinute = loginRequestsPerMinute;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String path = requestPath(request);
		String clientAddress = clientAddress(request);

		if (isApiRequest(path)) {
			if (!rateLimitService.tryAcquire(API_PREFIX + clientAddress, apiRequestsPerMinute, WINDOW)) {
				writeApiRateLimitResponse(request, response);
				return;
			}
		}
		else if (isLoginAttempt(request, path)) {
			if (!rateLimitService.tryAcquire(LOGIN_PREFIX + clientAddress, loginRequestsPerMinute, WINDOW)) {
				writeLoginRateLimitResponse(response);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private boolean isApiRequest(String path) {
		return path.equals("/api") || path.startsWith("/api/");
	}

	private boolean isLoginAttempt(HttpServletRequest request, String path) {
		return HttpMethod.POST.matches(request.getMethod()) && path.equals("/login");
	}

	private String requestPath(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		if (servletPath != null && !servletPath.isBlank()) {
			return servletPath;
		}
		return request.getRequestURI();
	}

	private String clientAddress(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",", 2)[0].trim();
		}
		String remoteAddress = request.getRemoteAddr();
		if (remoteAddress == null || remoteAddress.isBlank()) {
			return "unknown";
		}
		return remoteAddress;
	}

	private void writeApiRateLimitResponse(
			HttpServletRequest request,
			HttpServletResponse response
	) throws IOException {
		HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				"Too many requests. Please try again later.",
				request.getRequestURI()
		));
	}

	private void writeLoginRateLimitResponse(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.TEXT_PLAIN_VALUE);
		response.getWriter().write("Too many login attempts. Please try again later.");
	}
}
