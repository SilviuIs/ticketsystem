package com.example.ticketsystem.services;

import com.example.ticketsystem.models.ClassificationResult;
import com.example.ticketsystem.models.ClassificationRule;
import com.example.ticketsystem.models.ClassificationTerm;
import com.example.ticketsystem.repository.ClassificationRuleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassificationService {

	private final ClassificationRuleRepository ruleRepository;

	public ClassificationService(ClassificationRuleRepository ruleRepository) {
		this.ruleRepository = ruleRepository;
	}

	@Transactional(readOnly = true)
	public ClassificationResult classify(String title, String description) {
		// Titel und Beschreibung werden fuer die Suche vereinfacht.
		String normalizedText = normalize(title + " " + description);
		List<RuleScore> scores = ruleRepository.findByActiveTrue().stream()
				.map(rule -> scoreRule(rule, normalizedText))
				.sorted(Comparator.comparingInt(RuleScore::score).reversed())
				.toList();

		// Nur passende Regeln duerfen einen Vorschlag liefern.
		if (scores.isEmpty() || scores.getFirst().score() < scores.getFirst().rule().getThreshold()) {
			int topScore = scores.isEmpty() ? 0 : scores.getFirst().score();
			return new ClassificationResult(null, null, topScore, BigDecimal.ZERO, true,
					"Keine Regel hat den Mindestschwellenwert erreicht.");
		}

		RuleScore best = scores.getFirst();
		// Confidence zeigt, wie gut die Regel passt.
		BigDecimal confidence = BigDecimal.valueOf(best.score())
				.divide(BigDecimal.valueOf(best.rule().getThreshold()), 2, RoundingMode.HALF_UP)
				.min(BigDecimal.valueOf(1.00));

		String reason = "Regel '%s' erreicht %d Punkte durch: %s"
				.formatted(best.rule().getName(), best.score(), String.join(", ", best.matchedTerms()));

		return new ClassificationResult(best.rule().getCategory(), best.rule().getPriority(), best.score(), confidence, false, reason);
	}

	private RuleScore scoreRule(ClassificationRule rule, String normalizedText) {
		int score = 0;
		List<String> matchedTerms = new ArrayList<>();

		// Jeder aktive Begriff zaehlt einmal.
		for (ClassificationTerm term : rule.getTerms()) {
			if (!term.isActive()) {
				continue;
			}
			String normalizedTerm = normalize(term.getTerm());
			if (normalizedText.contains(normalizedTerm)) {
				score += term.getWeight();
				matchedTerms.add(term.getTerm() + " (+" + term.getWeight() + ")");
			}
		}

		return new RuleScore(rule, score, matchedTerms);
	}

	private String normalize(String input) {
		// Text für einfache Begriffssuche vorbereiten.
		String normalized = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFD);
		return normalized.replaceAll("\\p{M}", "")
				.toLowerCase(Locale.GERMAN)
				.replaceAll("[^a-z0-9äöüß]+", " ")
				.trim();
	}

	private record RuleScore(ClassificationRule rule, int score, List<String> matchedTerms) {
	}
}
