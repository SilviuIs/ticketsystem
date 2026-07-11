package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.ClassificationRule;
import com.example.ticketsystem.models.ClassificationRuleForm;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.ClassificationRuleRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassificationRuleAdminServiceTest {

	@Mock
	private ClassificationRuleRepository ruleRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private PriorityRepository priorityRepository;

	@Mock
	private AuditService auditService;

	@Mock
	private AuthenticatedUserService authenticatedUserService;

	@InjectMocks
	private ClassificationRuleAdminService ruleAdminService;

	@Test
	void createRuleParsesTokenWeightsFromFormText() {
		Category category = new Category("Netzwerk", "Verbindungen");
		Priority priority = new Priority("Level 1", 1, "Kritisch");
		ClassificationRuleForm form = new ClassificationRuleForm();
		form.setName("Netzwerk VPN");
		form.setCategoryId(1L);
		form.setPriorityId(2L);
		form.setThreshold(7);
		form.setActive(true);
		form.setTermsText("VPN:5\nwlan:4; internet:3");

		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(priorityRepository.findById(2L)).thenReturn(Optional.of(priority));
		when(ruleRepository.save(any(ClassificationRule.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(authenticatedUserService.currentUsername()).thenReturn("admin");

		ClassificationRule rule = ruleAdminService.createRule(form);

		assertThat(rule.getName()).isEqualTo("Netzwerk VPN");
		assertThat(rule.getThreshold()).isEqualTo(7);
		assertThat(rule.getCategory()).isEqualTo(category);
		assertThat(rule.getPriority()).isEqualTo(priority);
		assertThat(rule.getTerms())
				.extracting(term -> term.getTerm() + ":" + term.getWeight())
				.containsExactly("vpn:5", "wlan:4", "internet:3");
		verify(auditService).record(
				"CLASSIFICATION_RULE_CREATED",
				"admin",
				"ClassificationRule",
				rule.getId(),
				"Classification rule created: Netzwerk VPN"
		);
	}

	@Test
	void createRuleRejectsInvalidTokenWeight() {
		Category category = new Category("Netzwerk", "Verbindungen");
		Priority priority = new Priority("Level 1", 1, "Kritisch");
		ClassificationRuleForm form = new ClassificationRuleForm();
		form.setName("Netzwerk VPN");
		form.setCategoryId(1L);
		form.setPriorityId(2L);
		form.setThreshold(7);
		form.setActive(true);
		form.setTermsText("vpn:abc");

		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(priorityRepository.findById(2L)).thenReturn(Optional.of(priority));

		assertThatThrownBy(() -> ruleAdminService.createRule(form))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("ganze Zahlen");
	}

	@Test
	void createRuleRejectsDuplicateTermsBeforeDatabaseConstraint() {
		Category category = new Category("Netzwerk", "Verbindungen");
		Priority priority = new Priority("Level 1", 1, "Kritisch");
		ClassificationRuleForm form = new ClassificationRuleForm();
		form.setName("Netzwerk VPN");
		form.setCategoryId(1L);
		form.setPriorityId(2L);
		form.setThreshold(7);
		form.setActive(true);
		form.setTermsText("vpn:5\nVPN:3");

		when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
		when(priorityRepository.findById(2L)).thenReturn(Optional.of(priority));

		assertThatThrownBy(() -> ruleAdminService.createRule(form))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Begriff doppelt angegeben: vpn");
	}
}
