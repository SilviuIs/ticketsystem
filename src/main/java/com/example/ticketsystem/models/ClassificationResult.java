package com.example.ticketsystem.models;

import java.math.BigDecimal;

public record ClassificationResult(
		Category category,
		Priority priority,
		int score,
		BigDecimal confidenceLevel,
		boolean manualReviewRequired,
		String reason
) {
}
