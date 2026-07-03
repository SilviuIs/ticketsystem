package com.example.ticketsystem.services;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
		AppUser supportUser = new AppUser();
		supportUser.setUsername(SUPPORT_USERNAME);
		supportUser.setEmail("security-support@example.test");
		supportUser.setPasswordHash(passwordEncoder.encode(SUPPORT_PASSWORD));
		supportUser.getRoles().add(supportRole);
		userRepository.save(supportUser);
	}

	@Test
	void apiRequestWithoutCredentialsReturnsUnauthorizedInsteadOfLoginRedirect() throws Exception {
		mockMvc.perform(get("/api/tickets"))
				.andExpect(status().isUnauthorized())
				.andExpect(header().doesNotExist("Location"));
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
}
