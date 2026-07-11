package com.example.ticketsystem.services;

import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.models.ClassificationRule;
import com.example.ticketsystem.repository.ClassificationRuleRepository;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.repository.PriorityRepository;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.repository.AppUserRepository;
import com.example.ticketsystem.models.Role;
import com.example.ticketsystem.repository.RoleRepository;
import com.example.ticketsystem.repository.TicketRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("demo")
public class DemoDataInitializer implements CommandLineRunner {

	private final RoleRepository roleRepository;
	private final AppUserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final PriorityRepository priorityRepository;
	private final ClassificationRuleRepository ruleRepository;
	private final TicketRepository ticketRepository;
	private final TicketService ticketService;
	private final PasswordEncoder passwordEncoder;

	public DemoDataInitializer(
			RoleRepository roleRepository,
			AppUserRepository userRepository,
			CategoryRepository categoryRepository,
			PriorityRepository priorityRepository,
			ClassificationRuleRepository ruleRepository,
			TicketRepository ticketRepository,
			TicketService ticketService,
			PasswordEncoder passwordEncoder
	) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.categoryRepository = categoryRepository;
		this.priorityRepository = priorityRepository;
		this.ruleRepository = ruleRepository;
		this.ticketRepository = ticketRepository;
		this.ticketService = ticketService;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		// Vorhandene Demo-Daten bleiben erhalten.
		Role userRole = findOrCreateRole("USER");
		Role supportRole = findOrCreateRole("SUPPORT");
		Role adminRole = findOrCreateRole("ADMIN");

		AppUser user = findOrCreateUser("user", "user@example.local", "Max", "Mustermann", List.of(userRole));
		findOrCreateUser("support", "support@example.local", "Erika", "Support", List.of(supportRole));
		findOrCreateUser("admin", "admin@example.local", "Ada", "Admin", List.of(adminRole, supportRole));
		findOrCreateUser("mailbot", "mailbot@example.local", "Mail", "Import", List.of(userRole));

		Category hardware = findOrCreateCategory("Hardware", "Drucker, Notebook, Bildschirm und Peripherie");
		Category account = findOrCreateCategory("Account", "Passwort, Login und Benutzerzugriff");
		Category network = findOrCreateCategory("Netzwerk", "VPN, WLAN, LAN und Verbindungsprobleme");
		Category email = findOrCreateCategory("E-Mail", "Outlook, Postfach, Versand und Empfang");
		Category software = findOrCreateCategory("Software", "Installation, Updates, Programme und Lizenzen");
		Category permissions = findOrCreateCategory("Berechtigung", "Zugriff auf Ordner, Laufwerke und Anwendungen");
		Category security = findOrCreateCategory("Security", "Phishing, Malware und sicherheitsrelevante Vorfaelle");
		Category workplace = findOrCreateCategory("Arbeitsplatz", "Performance, Abstuerze und Arbeitsplatzgeraete");

		// Drei einfache Prioritaetsstufen.
		Priority level1 = findOrCreatePriority("Level 1", 1, "Kritischer Vorfall");
		Priority level2 = findOrCreatePriority("Level 2", 2, "Normaler Supportfall");
		Priority level3 = findOrCreatePriority("Level 3", 3, "Niedrige Prioritaet");

		// Beispielregeln fuer den ersten Start.
		createRuleIfMissing("Hardware Drucker", hardware, level2, 6, List.of(
				new TermSeed("drucker", 5),
				new TermSeed("papierstau", 4),
				new TermSeed("toner", 3)
		));
		createRuleIfMissing("Account Login", account, level1, 7, List.of(
				new TermSeed("passwort", 5),
				new TermSeed("login", 4),
				new TermSeed("gesperrt", 4)
		));
		createRuleIfMissing("Netzwerk Verbindung", network, level1, 7, List.of(
				new TermSeed("vpn", 5),
				new TermSeed("wlan", 4),
				new TermSeed("internet", 3),
				new TermSeed("verbindung", 3)
		));
		createRuleIfMissing("E-Mail Outlook", email, level2, 7, List.of(
				new TermSeed("outlook", 5),
				new TermSeed("email", 4),
				new TermSeed("mail", 4),
				new TermSeed("postfach", 4),
				new TermSeed("versenden", 3),
				new TermSeed("empfangen", 3),
				new TermSeed("anhang", 2)
		));
		createRuleIfMissing("Software Installation", software, level3, 7, List.of(
				new TermSeed("installation", 5),
				new TermSeed("installieren", 5),
				new TermSeed("software", 4),
				new TermSeed("programm", 4),
				new TermSeed("lizenz", 3),
				new TermSeed("update", 3)
		));
		createRuleIfMissing("Berechtigung Zugriff", permissions, level2, 7, List.of(
				new TermSeed("berechtigung", 5),
				new TermSeed("zugriff", 5),
				new TermSeed("freigabe", 4),
				new TermSeed("laufwerk", 4),
				new TermSeed("ordner", 3),
				new TermSeed("permission", 4)
		));
		createRuleIfMissing("Security Phishing", security, level1, 7, List.of(
				new TermSeed("phishing", 6),
				new TermSeed("verdacht", 4),
				new TermSeed("sicherheitswarnung", 5),
				new TermSeed("mail", 3),
				new TermSeed("link", 4),
				new TermSeed("virus", 5),
				new TermSeed("malware", 5)
		));
		createRuleIfMissing("Performance Arbeitsplatz", workplace, level2, 7, List.of(
				new TermSeed("langsam", 5),
				new TermSeed("performance", 4),
				new TermSeed("haengt", 4),
				new TermSeed("absturz", 4),
				new TermSeed("reagiert", 3),
				new TermSeed("startet", 3)
		));

		if (ticketRepository.count() == 0) {
			TicketForm demoTicket = new TicketForm();
			demoTicket.setTitle("Drucker funktioniert nicht");
			demoTicket.setDescription("Der Drucker im Buero hat Papierstau und druckt keine Dokumente mehr.");
			ticketService.createTicketForUser(demoTicket, user);
		}
	}

	private Role findOrCreateRole(String name) {
		return roleRepository.findByName(name)
				.orElseGet(() -> roleRepository.save(new Role(name)));
	}

	private AppUser findOrCreateUser(String username, String email, String firstName, String lastName, List<Role> roles) {
		return userRepository.findByUsername(username)
				.orElseGet(() -> {
					AppUser user = createUser(username, email, firstName, lastName);
					user.getRoles().addAll(roles);
					return userRepository.save(user);
				});
	}

	private Category findOrCreateCategory(String name, String description) {
		return categoryRepository.findByName(name)
				.orElseGet(() -> categoryRepository.save(new Category(name, description)));
	}

	private Priority findOrCreatePriority(String name, int level, String description) {
		return priorityRepository.findByName(name)
				.orElseGet(() -> priorityRepository.save(new Priority(name, level, description)));
	}

	private void createRuleIfMissing(String name, Category category, Priority priority, int threshold, List<TermSeed> terms) {
		if (ruleRepository.existsByName(name)) {
			return;
		}

		ClassificationRule rule = new ClassificationRule(name, category, priority, threshold);
		terms.forEach(term -> rule.addTerm(term.value(), term.weight()));
		ruleRepository.save(rule);
	}

	private AppUser createUser(String username, String email, String firstName, String lastName) {
		AppUser user = new AppUser();
		user.setUsername(username);
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode("password"));
		user.setFirstName(firstName);
		user.setLastName(lastName);
		return user;
	}

	private record TermSeed(String value, int weight) {
	}
}
