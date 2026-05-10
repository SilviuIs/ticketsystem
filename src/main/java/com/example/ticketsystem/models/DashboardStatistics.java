package com.example.ticketsystem.models;

import java.util.List;

public record DashboardStatistics(
		long totalTickets,
		long activeTickets,
		long waitingForUserTickets,
		long manualReviewTickets,
		long completedTickets,
		long unassignedTickets,
		List<StatisticItem> statusCounts,
		List<StatisticItem> categoryCounts,
		List<StatisticItem> priorityCounts
) {
}
