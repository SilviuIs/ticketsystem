package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.Comment;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.repository.CommentRepository;
import com.example.ticketsystem.repository.TicketStatusHistoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private TicketStatusHistoryRepository historyRepository;

	@Mock
	private TicketService ticketService;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@Mock
	private AuditService auditService;

	@InjectMocks
	private CommentService commentService;

	@Test
	void findByTicketChecksTicketVisibilityBeforeLoadingComments() {
		when(ticketService.findById(42L)).thenThrow(new AccessDeniedException("No access to ticket: 42"));

		assertThatThrownBy(() -> commentService.findByTicket(42L))
				.isInstanceOf(AccessDeniedException.class);

		verifyNoInteractions(commentRepository);
	}

	@Test
	void findByTicketReturnsCommentsWhenTicketIsVisible() {
		Ticket ticket = new Ticket();
		AppUser author = new AppUser();
		author.setUsername("support");
		Comment comment = new Comment(ticket, author, "Bitte pruefen");

		when(ticketService.findById(42L)).thenReturn(ticket);
		when(commentRepository.findByTicketIdOrderByCreatedAtAsc(42L)).thenReturn(List.of(comment));

		assertThat(commentService.findByTicket(42L)).containsExactly(comment);
		verify(ticketService).findById(42L);
	}
}
