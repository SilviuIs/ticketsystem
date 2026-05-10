package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.DashboardStatistics;
import com.example.ticketsystem.models.StatisticItem;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.repository.TicketRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@InjectMocks
	private DashboardService dashboardService;

	@Test
	void getStatisticsBuildsOperationalDashboardValues() {
		when(ticketRepository.count()).thenReturn(10L);
		when(ticketRepository.countByStatus(TicketStatus.OPEN)).thenReturn(3L);
		when(ticketRepository.countByStatus(TicketStatus.IN_PROGRESS)).thenReturn(2L);
		when(ticketRepository.countByStatus(TicketStatus.WAITING_FOR_USER)).thenReturn(1L);
		when(ticketRepository.countByStatus(TicketStatus.RESOLVED)).thenReturn(2L);
		when(ticketRepository.countByStatus(TicketStatus.CLOSED)).thenReturn(1L);
		when(ticketRepository.countByStatus(TicketStatus.MANUAL_REVIEW_REQUIRED)).thenReturn(1L);
		when(ticketRepository.countByStatusIn(anyCollection())).thenReturn(7L, 3L);
		when(ticketRepository.countByManualReviewRequiredTrue()).thenReturn(1L);
		when(ticketRepository.countByAssignedToIsNull()).thenReturn(4L);
		when(ticketRepository.countByFinalCategory()).thenReturn(List.of(new StatisticItem("Hardware", 6L)));
		when(ticketRepository.countByFinalPriority()).thenReturn(List.of(new StatisticItem("Level 1", 2L), new StatisticItem("Level 2", 8L)));

		DashboardStatistics statistics = dashboardService.getStatistics();

		assertThat(statistics.totalTickets()).isEqualTo(10);
		assertThat(statistics.activeTickets()).isEqualTo(7);
		assertThat(statistics.completedTickets()).isEqualTo(3);
		assertThat(statistics.waitingForUserTickets()).isEqualTo(1);
		assertThat(statistics.manualReviewTickets()).isEqualTo(1);
		assertThat(statistics.unassignedTickets()).isEqualTo(4);
		assertThat(statistics.statusCounts()).hasSize(TicketStatus.values().length);
		assertThat(statistics.categoryCounts()).containsExactly(new StatisticItem("Hardware", 6, 60));
		assertThat(statistics.priorityCounts()).containsExactly(
				new StatisticItem("Level 1", 2, 20),
				new StatisticItem("Level 2", 8, 80)
		);
	}
}
