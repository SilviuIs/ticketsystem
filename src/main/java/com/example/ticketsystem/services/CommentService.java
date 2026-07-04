package com.example.ticketsystem.services;

import com.example.ticketsystem.models.Comment;
import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.models.TicketStatusHistory;
import com.example.ticketsystem.repository.CommentRepository;
import com.example.ticketsystem.repository.TicketStatusHistoryRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

	private final CommentRepository commentRepository;
	private final TicketStatusHistoryRepository historyRepository;
	private final TicketService ticketService;
	private final AuthenticatedUserService authenticatedUserService;
	private final AuditService auditService;

	public CommentService(
			CommentRepository commentRepository,
			TicketStatusHistoryRepository historyRepository,
			TicketService ticketService,
			AuthenticatedUserService authenticatedUserService,
			AuditService auditService
	) {
		this.commentRepository = commentRepository;
		this.historyRepository = historyRepository;
		this.ticketService = ticketService;
		this.authenticatedUserService = authenticatedUserService;
		this.auditService = auditService;
	}

	@Transactional(readOnly = true)
	public List<Comment> findByTicket(Long ticketId) {
		return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
	}

	@Transactional
	public void addComment(Long ticketId, String content) {
		if (!authenticatedUserService.isSupportOrAdmin()) {
			throw new AccessDeniedException("Only support users can comment on tickets");
		}
		if (content == null || content.isBlank()) {
			return;
		}
		Ticket ticket = ticketService.findById(ticketId);
		AppUser author = authenticatedUserService.currentUser();
		commentRepository.save(new Comment(ticket, author, content.trim()));
		LOGGER.info("ticket_comment_added ticketId={} author={}", ticket.getId(), author.getUsername());
		auditService.record("TICKET_COMMENT_ADDED", author.getUsername(), "Ticket", ticket.getId(), "Support comment added");
	}

	@Transactional
	public void requestClarification(Long ticketId, String question) {
		if (!authenticatedUserService.isSupportOrAdmin()) {
			throw new AccessDeniedException("Only support users can request clarification");
		}
		if (question == null || question.isBlank()) {
			return;
		}

		Ticket ticket = ticketService.findById(ticketId);
		AppUser author = authenticatedUserService.currentUser();
		String trimmedQuestion = question.trim();
		TicketStatus oldStatus = ticket.getStatus();

		// Eine Rückfrage setzt das Ticket auf "wartet auf Benutzer".
		commentRepository.save(new Comment(ticket, author, "Rückfrage: " + trimmedQuestion));
		ticket.setStatus(TicketStatus.WAITING_FOR_USER);
		ticket.setAssignedTo(author);
		historyRepository.save(
				new TicketStatusHistory(ticket, oldStatus, TicketStatus.WAITING_FOR_USER, author, "Rückfrage an Benutzer gestellt")
		);
		LOGGER.info("ticket_clarification_requested ticketId={} requestedBy={}", ticket.getId(), author.getUsername());
		auditService.record("TICKET_CLARIFICATION_REQUESTED", author.getUsername(), "Ticket", ticket.getId(), "Clarification requested");
	}

	@Transactional
	public void answerClarification(Long ticketId, String answer) {
		if (answer == null || answer.isBlank()) {
			return;
		}

		Ticket ticket = ticketService.findById(ticketId);
		AppUser author = authenticatedUserService.currentUser();
		// Nur der Ticket-Ersteller darf antworten.
		if (!ticket.getCreatedBy().getUsername().equals(author.getUsername())
				|| authenticatedUserService.isSupportOrAdmin()) {
			throw new AccessDeniedException("Only the ticket creator can answer clarification requests");
		}
		if (ticket.getStatus() != TicketStatus.WAITING_FOR_USER) {
			throw new AccessDeniedException("This ticket is not waiting for a user answer");
		}

		// Nach der Antwort ist wieder Support dran.
		commentRepository.save(new Comment(ticket, author, "Antwort: " + answer.trim()));
		ticket.setStatus(TicketStatus.IN_PROGRESS);
		historyRepository.save(
				new TicketStatusHistory(ticket, TicketStatus.WAITING_FOR_USER, TicketStatus.IN_PROGRESS, author, "Antwort des Benutzers erhalten")
		);
		LOGGER.info("ticket_clarification_answered ticketId={} answeredBy={}", ticket.getId(), author.getUsername());
		auditService.record("TICKET_CLARIFICATION_ANSWERED", author.getUsername(), "Ticket", ticket.getId(), "Clarification answered");
	}
}
