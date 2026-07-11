package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.InboundMailMessage;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.repository.AppUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InboundMailTicketImportServiceTest {

	@Mock
	private InboundMailProperties properties;

	@Mock
	private AppUserRepository userRepository;

	@Mock
	private TicketService ticketService;

	@InjectMocks
	private InboundMailTicketImportService importService;

	@Test
	void createTicketFromMailUsesConfiguredTechnicalUser() {
		AppUser mailbot = new AppUser();
		mailbot.setUsername("mailbot");
		Ticket ticket = new Ticket();

		when(properties.getCreatedByUsername()).thenReturn("mailbot");
		when(userRepository.findByUsername("mailbot")).thenReturn(Optional.of(mailbot));
		when(ticketService.createTicketForUser(org.mockito.ArgumentMatchers.any(TicketForm.class), eq(mailbot)))
				.thenReturn(ticket);

		Ticket created = importService.createTicketFromMail(new InboundMailMessage(
				"VPN funktioniert nicht",
				"max@example.test",
				"Ich kann mich nicht verbinden."
		));

		ArgumentCaptor<TicketForm> formCaptor = ArgumentCaptor.forClass(TicketForm.class);
		verify(ticketService).createTicketForUser(formCaptor.capture(), eq(mailbot));
		assertThat(created).isSameAs(ticket);
		assertThat(formCaptor.getValue().getTitle()).isEqualTo("VPN funktioniert nicht");
		assertThat(formCaptor.getValue().getDescription())
				.contains("E-Mail Absender: max@example.test")
				.contains("Ich kann mich nicht verbinden.");
	}

	@Test
	void createTicketFromMailFailsWhenTechnicalUserIsMissing() {
		when(properties.getCreatedByUsername()).thenReturn("mailbot");
		when(userRepository.findByUsername("mailbot")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> importService.createTicketFromMail(new InboundMailMessage("Subject", "from", "body")))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("Inbound mail creator user not found: mailbot");
	}

	@Test
	void createTicketFromMailTruncatesLongSubject() {
		AppUser mailbot = new AppUser();
		mailbot.setUsername("mailbot");
		when(properties.getCreatedByUsername()).thenReturn("mailbot");
		when(userRepository.findByUsername("mailbot")).thenReturn(Optional.of(mailbot));

		importService.createTicketFromMail(new InboundMailMessage("x".repeat(300), "from", "body"));

		ArgumentCaptor<TicketForm> formCaptor = ArgumentCaptor.forClass(TicketForm.class);
		verify(ticketService).createTicketForUser(formCaptor.capture(), eq(mailbot));
		assertThat(formCaptor.getValue().getTitle()).hasSize(255).endsWith("...");
	}
}
