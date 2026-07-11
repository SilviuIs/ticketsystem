package com.example.ticketsystem.services;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = {
		"ticket-system.rate-limit.api.requests-per-minute=2",
		"ticket-system.rate-limit.login.requests-per-minute=2"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiRequestsAreRateLimitedPerClientAddress() throws Exception {
		String clientAddress = "203.0.113.10";

		mockMvc.perform(get("/api/tickets").with(remoteAddress(clientAddress)))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/tickets").with(remoteAddress(clientAddress)))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/tickets").with(remoteAddress(clientAddress)))
				.andExpect(status().isTooManyRequests())
				.andExpect(jsonPath("$.status").value(429))
				.andExpect(jsonPath("$.message").value("Too many requests. Please try again later."))
				.andExpect(jsonPath("$.path").value("/api/tickets"));
	}

	@Test
	void loginAttemptsAreRateLimitedPerClientAddress() throws Exception {
		String clientAddress = "203.0.113.11";

		mockMvc.perform(post("/login")
						.param("username", "missing-user")
						.param("password", "wrong-password")
						.with(csrf())
						.with(remoteAddress(clientAddress)))
				.andExpect(status().is3xxRedirection());
		mockMvc.perform(post("/login")
						.param("username", "missing-user")
						.param("password", "wrong-password")
						.with(csrf())
						.with(remoteAddress(clientAddress)))
				.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/login")
						.param("username", "missing-user")
						.param("password", "wrong-password")
						.with(csrf())
						.with(remoteAddress(clientAddress)))
				.andExpect(status().isTooManyRequests())
				.andExpect(content().string(containsString("Too many login attempts")));
	}

	private static RequestPostProcessor remoteAddress(String address) {
		return request -> {
			request.setRemoteAddr(address);
			return request;
		};
	}
}
