package com.example.ticketsystem.services;

import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.ClassificationRule;
import com.example.ticketsystem.models.ClassificationRuleForm;
import com.example.ticketsystem.models.ClassificationTerm;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.ClassificationRuleRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassificationRuleAdminService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClassificationRuleAdminService.class);

	private final ClassificationRuleRepository ruleRepository;
	private final CategoryRepository categoryRepository;
	private final PriorityRepository priorityRepository;

	public ClassificationRuleAdminService(
			ClassificationRuleRepository ruleRepository,
			CategoryRepository categoryRepository,
			PriorityRepository priorityRepository
	) {
		this.ruleRepository = ruleRepository;
		this.categoryRepository = categoryRepository;
		this.priorityRepository = priorityRepository;
	}

	@Transactional(readOnly = true)
	public List<ClassificationRule> findAllRules() {
		return ruleRepository.findAllWithDetails();
	}

	@Transactional(readOnly = true)
	public ClassificationRule findRule(Long id) {
		return ruleRepository.findByIdWithDetails(id)
				.orElseThrow(() -> new IllegalArgumentException("Classification rule not found: " + id));
	}

	@Transactional(readOnly = true)
	public ClassificationRuleForm toForm(Long id) {
		ClassificationRule rule = findRule(id);
		ClassificationRuleForm form = new ClassificationRuleForm();
		form.setName(rule.getName());
		form.setCategoryId(rule.getCategory().getId());
		form.setPriorityId(rule.getPriority().getId());
		form.setThreshold(rule.getThreshold());
		form.setActive(rule.isActive());
		form.setTermsText(toTermsText(rule));
		return form;
	}

	@Transactional
	public ClassificationRule createRule(ClassificationRuleForm form) {
		Category category = findCategory(form.getCategoryId());
		Priority priority = findPriority(form.getPriorityId());
		ClassificationRule rule = new ClassificationRule(form.getName().trim(), category, priority, form.getThreshold());
		rule.setActive(form.isActive());
		// Tokens aus dem Formular lesen.
		rule.replaceTerms(parseTerms(rule, form.getTermsText()));
		ClassificationRule saved = ruleRepository.save(rule);
		LOGGER.info("classification_rule_created id={} name={} active={}", saved.getId(), saved.getName(), saved.isActive());
		return saved;
	}

	@Transactional
	public void updateRule(Long id, ClassificationRuleForm form) {
		ClassificationRule rule = findRule(id);
		List<ClassificationTerm> parsedTerms = parseTerms(rule, form.getTermsText());
		rule.setName(form.getName().trim());
		rule.setCategory(findCategory(form.getCategoryId()));
		rule.setPriority(findPriority(form.getPriorityId()));
		rule.setThreshold(form.getThreshold());
		rule.setActive(form.isActive());
		// Alte Tokens zuerst entfernen, damit der Unique Key nicht stoert.
		rule.replaceTerms(List.of());
		ruleRepository.flush();
		rule.replaceTerms(parsedTerms);
		LOGGER.info("classification_rule_updated id={} name={} active={}", rule.getId(), rule.getName(), rule.isActive());
	}

	@Transactional
	public void toggleRule(Long id) {
		ClassificationRule rule = findRule(id);
		rule.setActive(!rule.isActive());
		LOGGER.info("classification_rule_toggled id={} name={} active={}", rule.getId(), rule.getName(), rule.isActive());
	}

	private Category findCategory(Long id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
	}

	private Priority findPriority(Long id) {
		return priorityRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Priority not found: " + id));
	}

	private List<ClassificationTerm> parseTerms(ClassificationRule rule, String termsText) {
		// Erlaubtes Format: begriff:gewicht.
		List<ClassificationTerm> terms = termsText.lines()
				.flatMap(line -> List.of(line.split("[,;]")).stream())
				.map(String::trim)
				.filter(entry -> !entry.isBlank())
				.map(entry -> parseTerm(rule, entry))
				.toList();

		if (terms.isEmpty()) {
			throw new IllegalArgumentException("Mindestens ein Begriff mit Gewichtung ist erforderlich.");
		}
		return terms;
	}

	private ClassificationTerm parseTerm(ClassificationRule rule, String entry) {
		String[] parts = entry.split(":", 2);
		if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
			throw new IllegalArgumentException("Begriffe muessen im Format 'begriff:gewicht' angegeben werden.");
		}

		try {
			int weight = Integer.parseInt(parts[1].trim());
			if (weight <= 0) {
				throw new IllegalArgumentException("Gewichtungen muessen groesser als 0 sein.");
			}
			return new ClassificationTerm(rule, parts[0].trim().toLowerCase(), weight);
		}
		catch (NumberFormatException exception) {
			throw new IllegalArgumentException("Gewichtungen muessen ganze Zahlen sein.");
		}
	}

	private String toTermsText(ClassificationRule rule) {
		return rule.getTerms().stream()
				.sorted(Comparator.comparing(ClassificationTerm::getTerm))
				.map(term -> term.getTerm() + ":" + term.getWeight())
				.reduce((left, right) -> left + "\n" + right)
				.orElse("");
	}
}
