package com.example.ticketsystem.models;

public record StatisticItem(String label, long count, int percentage) {

	public StatisticItem(String label, Long count) {
		this(label, count == null ? 0 : count, 0);
	}

	public StatisticItem withPercentage(long total) {
		if (total <= 0 || count <= 0) {
			return new StatisticItem(label, count, 0);
		}
		return new StatisticItem(label, count, (int) Math.round((count * 100.0) / total));
	}
}
