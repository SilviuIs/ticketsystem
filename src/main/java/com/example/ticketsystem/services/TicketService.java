package com.example.ticketsystem.services;

import com.example.ticketsystem.models.ClassificationResult;
import com.example.ticketsystem.models.AppUser;
import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.FinalClassificationForm;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.models.TicketStatusHistory;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import com.example.ticketsystem.repository.TicketRepository;
import com.example.ticketsystem.repository.TicketStatusHistoryRepository;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

	private final TicketRepository ticketRepository;
	private final TicketStatusHistoryRepository historyRepository;
	private final ClassificationService classificationService;
	private final AuthenticatedUserService authenticatedUserService;
	private final CategoryRepository categoryRepository;
	private final PriorityRepository priorityRepository;

	public TicketService(
			TicketRepository ticketRepository,
			TicketStatusHistoryRepository historyRepository,
			ClassificationService classificationService,
			AuthenticatedUserService authenticatedUserService,
			CategoryRepository categoryRepository,
			PriorityRepository priorityRepository
	) {
		this.ticketRepository = ticketRepository;
		this.historyRepository = historyRepository;
		this.classificationService = classificationService;
		this.authenticatedUserService = authenticatedUserService;
		this.categoryRepository = categoryRepository;
		this.priorityRepository = priorityRepository;
	}

	@Transactional(readOnly = true)
	public List<Ticket> findVisibleTickets() {
		// Support und Admin sehen alle Tickets. User sehen nur eigene Tickets.
		if (authenticatedUserService.isSupportOrAdmin()) {
			return ticketRepository.findAll();
		}
		return ticketRepository.findByCreatedByUsername(authenticatedUserService.currentUsername());
	}

	@Transactional(readOnly = true)
	public Ticket findById(Long id) {
		Ticket ticket = ticketRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
		if (!canView(ticket)) {
			throw new AccessDeniedException("No access to ticket: " + id);
		}
		return ticket;
	}

	@Transactional
	public Ticket createTicket(TicketForm form) {
		return createTicketForUser(form, authenticatedUserService.currentUser());
	}

	@Transactional
	public Ticket createTicketForUser(TicketForm form, AppUser creator) {
		Ticket ticket = new Ticket();
		ticket.setTitle(form.getTitle());
		ticket.setDescription(form.getDescription());
		ticket.setCreatedBy(creator);

		// Neue Tickets bekommen sofort einen Klassifikationsvorschlag.
		ClassificationResult result = classificationService.classify(form.getTitle(), form.getDescription());
		ticket.setSuggestedCategory(result.category());
		ticket.setSuggestedPriority(result.priority());
		ticket.setFinalCategory(result.category());
		ticket.setFinalPriority(result.priority());
		ticket.setClassificationScore(result.score());
		ticket.setConfidenceLevel(result.confidenceLevel());
		ticket.setManualReviewRequired(result.manualReviewRequired());
		ticket.setClassificationReason(result.reason());
		if (result.manualReviewRequired()) {
			// Ohne sicheren Treffer muss Support manuell pruefen.
			ticket.setStatus(TicketStatus.MANUAL_REVIEW_REQUIRED);
		}

		Ticket saved = ticketRepository.save(ticket);
		historyRepository.save(new TicketStatusHistory(saved, null, saved.getStatus(), creator, "Ticket erstellt"));
		return saved;
	}

	@Transactional
	public void changeStatus(Long ticketId, TicketStatus newStatus, String note) {
		if (!authenticatedUserService.isSupportOrAdmin()) {
			throw new AccessDeniedException("Only support users can change ticket status");
		}
		Ticket ticket = findById(ticketId);
		TicketStatus oldStatus = ticket.getStatus();
		if (oldStatus == newStatus) {
			return;
		}
		AppUser supportUser = authenticatedUserService.currentUser();
		// Der aktuelle Support-Benutzer wird als Bearbeiter gesetzt.
		ticket.setStatus(newStatus);
		ticket.setAssignedTo(supportUser);
		historyRepository.save(new TicketStatusHistory(ticket, oldStatus, newStatus, supportUser, note));
	}

	@Transactional
	public void updateFinalClassification(Long ticketId, FinalClassificationForm form) {
		if (!authenticatedUserService.isSupportOrAdmin()) {
			throw new AccessDeniedException("Only support users can set final classification");
		}

		Ticket ticket = findById(ticketId);
		Category category = categoryRepository.findById(form.getFinalCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Category not found: " + form.getFinalCategoryId()));
		Priority priority = priorityRepository.findById(form.getFinalPriorityId())
				.orElseThrow(() -> new IllegalArgumentException("Priority not found: " + form.getFinalPriorityId()));
		AppUser supportUser = authenticatedUserService.currentUser();
		TicketStatus oldStatus = ticket.getStatus();
		TicketStatus newStatus = oldStatus == TicketStatus.MANUAL_REVIEW_REQUIRED ? TicketStatus.OPEN : oldStatus;

		// Der Vorschlag bleibt erhalten. Nur die finale Auswahl wird geaendert.
		ticket.setFinalCategory(category);
		ticket.setFinalPriority(priority);
		ticket.setManualReviewRequired(false);
		ticket.setAssignedTo(supportUser);
		ticket.setStatus(newStatus);

		historyRepository.save(new TicketStatusHistory(ticket, oldStatus, newStatus, supportUser, buildFinalClassificationNote(category, priority, form.getNote())));
	}

	@Transactional(readOnly = true)
	public List<TicketStatusHistory> findHistory(Long ticketId) {
		// findById prueft auch die Sichtbarkeit.
		findById(ticketId);
		return historyRepository.findByTicketIdOrderByChangedAtDesc(ticketId);
	}

	private String buildFinalClassificationNote(Category category, Priority priority, String note) {
		String message = "Finale Klassifikation gesetzt: " + category.getName() + " / " + priority.getName();
		if (note == null || note.isBlank()) {
			return message;
		}
		return message + ". " + note.trim();
	}

	private boolean canView(Ticket ticket) {
		// User duerfen keine fremden Tickets sehen.
		return authenticatedUserService.isSupportOrAdmin()
				|| ticket.getCreatedBy().getUsername().equals(authenticatedUserService.currentUsername());
	}
}
