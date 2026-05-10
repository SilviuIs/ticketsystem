package com.example.ticketsystem.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/css/**", "/login").permitAll()
						.requestMatchers("/dashboard").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/dashboard").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/classification/preview").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/tickets/*/status", "/api/tickets/*/classification").hasAnyRole("SUPPORT", "ADMIN")
						.requestMatchers("/api/**").authenticated()
						.requestMatchers("/admin/**").hasRole("ADMIN")
						// Diese Ticketaktionen sind nur fuer Support oder Admin.
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
				.httpBasic(Customizer.withDefaults())
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
