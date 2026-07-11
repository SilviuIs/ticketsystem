package com.example.ticketsystem.services;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.InboundMailMessage;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InboundMailTicketImportService {

	private static final int MAX_TITLE_LENGTH = 255;
	private static final int MAX_DESCRIPTION_LENGTH = 4000;

	private final InboundMailProperties properties;
	private final AppUserRepository userRepository;
	private final TicketService ticketService;

	public InboundMailTicketImportService(
			InboundMailProperties properties,
			AppUserRepository userRepository,
			TicketService ticketService
	) {
		this.properties = properties;
		this.userRepository = userRepository;
		this.ticketService = ticketService;
	}

	@Transactional
	public Ticket createTicketFromMail(InboundMailMessage message) {
		AppUser creator = userRepository.findByUsername(properties.getCreatedByUsername())
				.orElseThrow(() -> new IllegalStateException(
						"Inbound mail creator user not found: " + properties.getCreatedByUsername()));

		TicketForm form = new TicketForm();
		form.setTitle(truncate(blankToDefault(message.subject(), "E-Mail ohne Betreff"), MAX_TITLE_LENGTH));
		form.setDescription(truncate(buildDescription(message), MAX_DESCRIPTION_LENGTH));
		return ticketService.createTicketForUser(form, creator);
	}

	private String buildDescription(InboundMailMessage message) {
		return """
				E-Mail Absender: %s

				%s
				""".formatted(
				blankToDefault(message.from(), "unbekannt"),
				blankToDefault(message.body(), "Kein Nachrichtentext vorhanden.")
		).trim();
	}

	private String blankToDefault(String value, String fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return value.trim();
	}

	private String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		if (maxLength <= 3) {
			return value.substring(0, maxLength);
		}
		return value.substring(0, maxLength - 3) + "...";
	}
}
