package com.example.ticketsystem.services;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.Role;
import com.example.ticketsystem.repository.AppUserRepository;
import com.example.ticketsystem.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

	private static final String SUPPORT_USERNAME = "security-support";
	private static final String SUPPORT_PASSWORD = "password";
	private static final String USER_USERNAME = "security-user";
	private static final String USER_PASSWORD = "password";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppUserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		roleRepository.deleteAll();

		Role supportRole = roleRepository.save(new Role("ROLE_SUPPORT"));
		Role userRole = roleRepository.save(new Role("ROLE_USER"));
		userRepository.save(createUser(SUPPORT_USERNAME, "security-support@example.test", SUPPORT_PASSWORD, supportRole));
		userRepository.save(createUser(USER_USERNAME, "security-user@example.test", USER_PASSWORD, userRole));
	}

	@Test
	void apiRequestWithoutCredentialsReturnsUnauthorizedInsteadOfLoginRedirect() throws Exception {
		mockMvc.perform(get("/api/tickets"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().doesNotExist("Location"))
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Authentication required"))
				.andExpect(jsonPath("$.path").value("/api/tickets"));
	}

	@Test
	void webRequestWithoutCredentialsStillRedirectsToLogin() throws Exception {
		mockMvc.perform(get("/tickets"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void apiPostWithBasicAuthenticationDoesNotRequireCsrfToken() throws Exception {
		mockMvc.perform(post("/api/classification/preview")
						.with(httpBasic(SUPPORT_USERNAME, SUPPORT_PASSWORD))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "VPN funktioniert nicht",
								  "description": "Benutzer kann sich nicht verbinden"
								}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void apiValidationErrorUsesStandardErrorResponse() throws Exception {
		mockMvc.perform(post("/api/tickets")
						.with(httpBasic(SUPPORT_USERNAME, SUPPORT_PASSWORD))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "",
								  "description": ""
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Invalid request"))
				.andExpect(jsonPath("$.fieldErrors").isArray())
				.andExpect(jsonPath("$.fieldErrors.length()").value(2));
	}

	@Test
	void apiNotFoundErrorUsesStandardErrorResponse() throws Exception {
		mockMvc.perform(get("/api/tickets/999999")
						.with(httpBasic(SUPPORT_USERNAME, SUPPORT_PASSWORD)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Ticket not found: 999999"))
				.andExpect(jsonPath("$.path").value("/api/tickets/999999"));
	}

	@Test
	void apiForbiddenErrorUsesStandardErrorResponse() throws Exception {
		mockMvc.perform(get("/api/dashboard")
						.with(httpBasic(USER_USERNAME, USER_PASSWORD)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("Access denied"))
				.andExpect(jsonPath("$.path").value("/api/dashboard"));
	}

	@Test
	void actuatorHealthIsPublicButMetricsRequiresLogin() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/actuator/metrics"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	private AppUser createUser(String username, String email, String password, Role role) {
		AppUser user = new AppUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.getRoles().add(role);
		return user;
	}
}
