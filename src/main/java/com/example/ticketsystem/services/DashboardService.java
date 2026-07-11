package com.example.ticketsystem.services;

import com.example.ticketsystem.models.DashboardStatistics;
import com.example.ticketsystem.models.StatisticItem;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.repository.TicketRepository;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
		Map<TicketStatus, Long> statusCounts = loadStatusCounts();
		long waitingForUserTickets = statusCounts.getOrDefault(TicketStatus.WAITING_FOR_USER, 0L);

		return new DashboardStatistics(
				totalTickets,
				sumStatusCounts(statusCounts, ACTIVE_STATUSES),
				waitingForUserTickets,
				ticketRepository.countByManualReviewRequiredTrue(),
				sumStatusCounts(statusCounts, COMPLETED_STATUSES),
				ticketRepository.countByAssignedToIsNull(),
				buildStatusCounts(statusCounts, totalTickets),
				withPercentages(ticketRepository.countByFinalCategory(), totalTickets),
				withPercentages(ticketRepository.countByFinalPriority(), totalTickets)
		);
	}

	private Map<TicketStatus, Long> loadStatusCounts() {
		Map<TicketStatus, Long> statusCounts = new EnumMap<>(TicketStatus.class);
		for (TicketRepository.StatusCount statusCount : ticketRepository.countByStatusGroup()) {
			statusCounts.put(statusCount.getStatus(), statusCount.getCount());
		}
		return statusCounts;
	}

	private long sumStatusCounts(Map<TicketStatus, Long> statusCounts, Collection<TicketStatus> statuses) {
		return statuses.stream()
				.mapToLong(status -> statusCounts.getOrDefault(status, 0L))
				.sum();
	}

	private List<StatisticItem> buildStatusCounts(Map<TicketStatus, Long> statusCounts, long totalTickets) {
		// Auch Status ohne Tickets sollen im Dashboard sichtbar sein.
		return Stream.of(TicketStatus.values())
				.map(status -> new StatisticItem(statusLabel(status), statusCounts.getOrDefault(status, 0L)).withPercentage(totalTickets))
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
