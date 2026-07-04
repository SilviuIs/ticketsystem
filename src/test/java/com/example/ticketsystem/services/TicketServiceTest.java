package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.ClassificationResult;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.models.TicketStatusHistory;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import com.example.ticketsystem.repository.TicketRepository;
import com.example.ticketsystem.repository.TicketStatusHistoryRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketStatusHistoryRepository historyRepository;

	@Mock
	private ClassificationService classificationService;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private PriorityRepository priorityRepository;

	@InjectMocks
	private TicketService ticketService;

	@Test
	void findVisibleTicketsReturnsAllTicketsForSupport() {
		Ticket ticket = new Ticket();
		when(authenticatedUserService.isSupportOrAdmin()).thenReturn(true);
		when(ticketRepository.findAll()).thenReturn(List.of(ticket));

		assertThat(ticketService.findVisibleTickets()).containsExactly(ticket);
		verify(ticketRepository).findAll();
	}

	@Test
	void findVisibleTicketsReturnsOnlyOwnTicketsForNormalUser() {
		Ticket ticket = new Ticket();
		when(authenticatedUserService.isSupportOrAdmin()).thenReturn(false);
		when(authenticatedUserService.currentUsername()).thenReturn("user");
		when(ticketRepository.findByCreatedByUsername("user")).thenReturn(List.of(ticket));

		assertThat(ticketService.findVisibleTickets()).containsExactly(ticket);
		verify(ticketRepository).findByCreatedByUsername("user");
	}

	@Test
	void findVisibleTicketsPageReturnsAllTicketsForSupport() {
		Ticket ticket = new Ticket();
		PageRequest pageRequest = PageRequest.of(0, 10);
		when(authenticatedUserService.isSupportOrAdmin()).thenReturn(true);
		when(ticketRepository.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(ticket), pageRequest, 1));

		assertThat(ticketService.findVisibleTickets(pageRequest).getContent()).containsExactly(ticket);
		verify(ticketRepository).findAll(pageRequest);
	}

	@Test
	void findVisibleTicketsPageReturnsOnlyOwnTicketsForNormalUser() {
		Ticket ticket = new Ticket();
		PageRequest pageRequest = PageRequest.of(0, 10);
		when(authenticatedUserService.isSupportOrAdmin()).thenReturn(false);
		when(authenticatedUserService.currentUsername()).thenReturn("user");
		when(ticketRepository.findByCreatedByUsername("user", pageRequest)).thenReturn(new PageImpl<>(List.of(ticket), pageRequest, 1));

		assertThat(ticketService.findVisibleTickets(pageRequest).getContent()).containsExactly(ticket);
		verify(ticketRepository).findByCreatedByUsername("user", pageRequest);
	}

	@Test
	void createTicketStoresClassificationSuggestionAndHistory() {
		AppUser creator = new AppUser();
		creator.setUsername("user");
		Category category = new Category("Hardware", "Geraete");
		Priority priority = new Priority("Level 2", 2, "Normal");
		TicketForm form = new TicketForm();
		form.setTitle("Drucker defekt");
		form.setDescription("Papierstau am Drucker");

		when(authenticatedUserService.currentUser()).thenReturn(creator);
		when(classificationService.classify(form.getTitle(), form.getDescription()))
				.thenReturn(new ClassificationResult(category, priority, 9, BigDecimal.ONE, false, "Regel erreicht"));
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Ticket ticket = ticketService.createTicket(form);

		assertThat(ticket.getCreatedBy()).isEqualTo(creator);
		assertThat(ticket.getSuggestedCategory()).isEqualTo(category);
		assertThat(ticket.getFinalCategory()).isEqualTo(category);
		assertThat(ticket.getSuggestedPriority()).isEqualTo(priority);
		assertThat(ticket.getFinalPriority()).isEqualTo(priority);
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
		assertThat(ticket.isManualReviewRequired()).isFalse();
		verify(historyRepository).save(any(TicketStatusHistory.class));
	}

	@Test
	void createTicketMarksManualReviewWhenClassificationIsUncertain() {
		AppUser creator = new AppUser();
		creator.setUsername("user");
		TicketForm form = new TicketForm();
		form.setTitle("Unklare Meldung");
		form.setDescription("Bitte pruefen");

		when(authenticatedUserService.currentUser()).thenReturn(creator);
		when(classificationService.classify(form.getTitle(), form.getDescription()))
				.thenReturn(new ClassificationResult(null, null, 0, BigDecimal.ZERO, true, "Keine Regel"));
		when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Ticket ticket = ticketService.createTicket(form);

		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.MANUAL_REVIEW_REQUIRED);
		assertThat(ticket.isManualReviewRequired()).isTrue();
		verify(historyRepository).save(any(TicketStatusHistory.class));
	}
}
