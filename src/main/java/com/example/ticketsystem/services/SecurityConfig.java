package com.example.ticketsystem.services;

import com.example.ticketsystem.models.ApiDtos.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	@Order(1)
	SecurityFilterChain apiSecurityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		return http
				.securityMatcher("/api/**")
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.GET, "/api/dashboard")
						.hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/classification/preview")
						.hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/tickets/*/status", "/api/tickets/*/classification")
						.hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers("/api/**").authenticated()
				)
				.httpBasic(Customizer.withDefaults())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) ->
								writeApiError(request, response, objectMapper, HttpStatus.UNAUTHORIZED, "Authentication required"))
						.accessDeniedHandler((request, response, exception) ->
								writeApiError(request, response, objectMapper, HttpStatus.FORBIDDEN, "Access denied"))
				)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.requestCache(AbstractHttpConfigurer::disable)
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/css/**", "/login").permitAll()
						.requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
						.requestMatchers("/actuator/**").hasRole("ADMIN")
						.requestMatchers("/dashboard")
						.hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// Diese Ticketaktionen sind nur für Support oder Admin.
						.requestMatchers("/tickets/*/classification").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers("/tickets/*/clarification").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers("/tickets/*/status").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers("/tickets/*/comments").hasAnyRole("SUPPORT", "ADMIN")
						.anyRequest().authenticated()
				)
				.formLogin(login -> login
						.loginPage("/login")
						.defaultSuccessUrl("/tickets", true)
						.permitAll()
				)
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.permitAll()
				)
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	ObjectMapper objectMapper() {
		return JsonMapper.builder()
				.findAndAddModules()
				.build();
	}

	private void writeApiError(
			HttpServletRequest request,
			HttpServletResponse response,
			ObjectMapper objectMapper,
			HttpStatus status,
			String message
	) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		));
	}
}
