package com.example.ticketsystem.services;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.repository.AppUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final AppUserRepository userRepository;

	public DatabaseUserDetailsService(AppUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser appUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("Unknown user: " + username));

		// .roles(...) erwartet Namen ohne ROLE_-Praefix.
		String[] roles = appUser.getRoles().stream()
				.map(role -> role.getName().replaceFirst("^ROLE_", ""))
				.toArray(String[]::new);

		return User.withUsername(appUser.getUsername())
				.password(appUser.getPasswordHash())
				.disabled(!appUser.isEnabled())
				.roles(roles)
				.build();
	}
}
