package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.ClassificationResult;
import com.example.ticketsystem.models.ClassificationRule;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.repository.ClassificationRuleRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassificationServiceTest {

	@Mock
	private ClassificationRuleRepository ruleRepository;

	@InjectMocks
	private ClassificationService classificationService;

	@Test
	void classifyReturnsBestMatchingRuleWhenThresholdIsReached() {
		Category hardware = new Category("Hardware", "Geraete und Peripherie");
		Priority level2 = new Priority("Level 2", 2, "Normaler Supportfall");
		ClassificationRule printerRule = new ClassificationRule("Hardware Drucker", hardware, level2, 6);
		printerRule.addTerm("drucker", 5);
		printerRule.addTerm("papierstau", 4);

		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(printerRule));

		ClassificationResult result = classificationService.classify(
				"Drucker funktioniert nicht",
				"Der Drucker hat Papierstau und druckt nicht mehr."
		);

		assertThat(result.category()).isEqualTo(hardware);
		assertThat(result.priority()).isEqualTo(level2);
		assertThat(result.score()).isEqualTo(9);
		assertThat(result.confidenceLevel()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
		assertThat(result.manualReviewRequired()).isFalse();
		assertThat(result.reason()).contains("Hardware Drucker", "drucker", "papierstau");
	}

	@Test
	void classifyRequiresManualReviewWhenNoRuleReachesThreshold() {
		Category network = new Category("Netzwerk", "Verbindungsprobleme");
		Priority level1 = new Priority("Level 1", 1, "Kritischer Vorfall");
		ClassificationRule networkRule = new ClassificationRule("Netzwerk Verbindung", network, level1, 7);
		networkRule.addTerm("vpn", 5);

		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(networkRule));

		ClassificationResult result = classificationService.classify(
				"VPN langsam",
				"Die Verbindung ist instabil."
		);

		assertThat(result.category()).isNull();
		assertThat(result.priority()).isNull();
		assertThat(result.score()).isEqualTo(5);
		assertThat(result.confidenceLevel()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.manualReviewRequired()).isTrue();
		assertThat(result.reason()).contains("Mindestschwellenwert");
	}

	@Test
	void classifyIgnoresInvalidZeroThresholdRule() {
		Category hardware = new Category("Hardware", "Geraete und Peripherie");
		Priority level2 = new Priority("Level 2", 2, "Normaler Supportfall");
		ClassificationRule invalidRule = new ClassificationRule("Invalid Rule", hardware, level2, 0);
		invalidRule.addTerm("drucker", 5);

		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(invalidRule));

		ClassificationResult result = classificationService.classify("Drucker", "Drucker defekt");

		assertThat(result.category()).isNull();
		assertThat(result.priority()).isNull();
		assertThat(result.score()).isEqualTo(0);
		assertThat(result.confidenceLevel()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.manualReviewRequired()).isTrue();
	}

	@Test
	void classifyDoesNotMatchTermInsideAnotherWord() {
		Category network = new Category("Netzwerk", "Verbindungsprobleme");
		Priority level1 = new Priority("Level 1", 1, "Kritischer Vorfall");
		ClassificationRule networkRule = new ClassificationRule("Netzwerk VPN", network, level1, 5);
		networkRule.addTerm("vpn", 5);

		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(networkRule));

		ClassificationResult result = classificationService.classify(
				"OpenVPN Client",
				"Der Client startet nicht."
		);

		assertThat(result.category()).isNull();
		assertThat(result.score()).isZero();
		assertThat(result.manualReviewRequired()).isTrue();
	}

	@Test
	void classifyMatchesMultiWordTermsWithWordBoundaries() {
		Category account = new Category("Account", "Loginprobleme");
		Priority level1 = new Priority("Level 1", 1, "Kritischer Vorfall");
		ClassificationRule accountRule = new ClassificationRule("Account Passwort", account, level1, 6);
		accountRule.addTerm("passwort vergessen", 6);

		when(ruleRepository.findByActiveTrue()).thenReturn(List.of(accountRule));

		ClassificationResult result = classificationService.classify(
				"Passwort vergessen",
				"Ich kann mich nicht mehr anmelden."
		);

		assertThat(result.category()).isEqualTo(account);
		assertThat(result.priority()).isEqualTo(level1);
		assertThat(result.score()).isEqualTo(6);
		assertThat(result.manualReviewRequired()).isFalse();
	}
}
