package com.example.ticketsystem.services;

import com.example.ticketsystem.models.DashboardStatistics;
import com.example.ticketsystem.models.StatisticItem;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.repository.TicketRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

	private static final List<TicketStatus> ACTIVE_STATUSES = List.of(
			TicketStatus.OPEN,
			TicketStatus.IN_PROGRESS,
			TicketStatus.WAITING_FOR_USER,
			TicketStatus.MANUAL_REVIEW_REQUIRED
	);
	private static final List<TicketStatus> COMPLETED_STATUSES = List.of(TicketStatus.RESOLVED, TicketStatus.CLOSED);

	private final TicketRepository ticketRepository;

	public DashboardService(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	@Transactional(readOnly = true)
	public DashboardStatistics getStatistics() {
		long totalTickets = ticketRepository.count();
		long waitingForUserTickets = ticketRepository.countByStatus(TicketStatus.WAITING_FOR_USER);

		return new DashboardStatistics(
				totalTickets,
				ticketRepository.countByStatusIn(ACTIVE_STATUSES),
				waitingForUserTickets,
				ticketRepository.countByManualReviewRequiredTrue(),
				ticketRepository.countByStatusIn(COMPLETED_STATUSES),
				ticketRepository.countByAssignedToIsNull(),
				buildStatusCounts(totalTickets),
				withPercentages(ticketRepository.countByFinalCategory(), totalTickets),
				withPercentages(ticketRepository.countByFinalPriority(), totalTickets)
		);
	}

	private List<StatisticItem> buildStatusCounts(long totalTickets) {
		// Auch Status ohne Tickets sollen im Dashboard sichtbar sein.
		return Stream.of(TicketStatus.values())
				.map(status -> new StatisticItem(statusLabel(status), ticketRepository.countByStatus(status)).withPercentage(totalTickets))
				.toList();
	}

	private List<StatisticItem> withPercentages(Collection<StatisticItem> items, long totalTickets) {
		return items.stream()
				.map(item -> item.withPercentage(totalTickets))
				.toList();
	}

	private String statusLabel(TicketStatus status) {
		return switch (status) {
			case OPEN -> "Offen";
			case IN_PROGRESS -> "In Bearbeitung";
			case WAITING_FOR_USER -> "Wartet auf Benutzer";
			case RESOLVED -> "Geloest";
			case CLOSED -> "Geschlossen";
			case MANUAL_REVIEW_REQUIRED -> "Manuelle Pruefung";
		};
	}
}
