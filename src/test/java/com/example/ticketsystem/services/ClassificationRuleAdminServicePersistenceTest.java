package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.ClassificationRule;
import com.example.ticketsystem.models.ClassificationRuleForm;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.ClassificationRuleRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ClassificationRuleAdminServicePersistenceTest {

	@Autowired
	private ClassificationRuleAdminService ruleAdminService;

	@Autowired
	private ClassificationRuleRepository ruleRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private PriorityRepository priorityRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void updateRuleCanChangeWeightOfExistingToken() {
		String suffix = UUID.randomUUID().toString();
		int testPriorityLevel = priorityRepository.findAll().stream()
				.mapToInt(Priority::getLevel)
				.max()
				.orElse(0) + 1000;
		Category category = categoryRepository.save(new Category("Test Kategorie " + suffix, "Testdaten"));
		Priority priority = priorityRepository.save(new Priority("Test Prioritaet " + suffix, testPriorityLevel, "Testdaten"));
		ClassificationRule rule = new ClassificationRule("Test Regel " + suffix, category, priority, 5);
		rule.addTerm("vpn", 5);
		rule = ruleRepository.saveAndFlush(rule);

		ClassificationRuleForm form = new ClassificationRuleForm();
		form.setName(rule.getName());
		form.setCategoryId(category.getId());
		form.setPriorityId(priority.getId());
		form.setThreshold(5);
		form.setActive(true);
		form.setTermsText("vpn:9");

		ruleAdminService.updateRule(rule.getId(), form);
		entityManager.flush();
		entityManager.clear();

		ClassificationRule updatedRule = ruleAdminService.findRule(rule.getId());
		assertThat(updatedRule.getTerms())
				.singleElement()
				.satisfies(term -> {
					assertThat(term.getTerm()).isEqualTo("vpn");
					assertThat(term.getWeight()).isEqualTo(9);
				});
	}
}
