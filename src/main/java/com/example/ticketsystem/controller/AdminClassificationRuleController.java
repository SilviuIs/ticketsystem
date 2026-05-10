package com.example.ticketsystem.controller;

import com.example.ticketsystem.models.ClassificationRuleForm;
import com.example.ticketsystem.models.ClassificationResult;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import com.example.ticketsystem.services.ClassificationRuleAdminService;
import com.example.ticketsystem.services.ClassificationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/classification-rules")
public class AdminClassificationRuleController {

	private final ClassificationRuleAdminService ruleAdminService;
	private final ClassificationService classificationService;
	private final CategoryRepository categoryRepository;
	private final PriorityRepository priorityRepository;

	public AdminClassificationRuleController(
			ClassificationRuleAdminService ruleAdminService,
			ClassificationService classificationService,
			CategoryRepository categoryRepository,
			PriorityRepository priorityRepository
	) {
		this.ruleAdminService = ruleAdminService;
		this.classificationService = classificationService;
		this.categoryRepository = categoryRepository;
		this.priorityRepository = priorityRepository;
	}

	@ModelAttribute
	void addReferenceData(Model model) {
		// Auswahlwerte fuer die Formulare.
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("priorities", priorityRepository.findAll());
	}

	@GetMapping
	public String list(Model model) {
		addRules(model);
		return "admin/classification-rules/list";
	}

	@GetMapping("/new")
	public String newRule(Model model) {
		if (!model.containsAttribute("classificationRuleForm")) {
			ClassificationRuleForm form = new ClassificationRuleForm();
			// Kleine Beispielwerte fuer neue Regeln.
			form.setThreshold(5);
			form.setTermsText("drucker:5\npapierstau:4");
			model.addAttribute("classificationRuleForm", form);
		}
		model.addAttribute("formMode", "create");
		return "admin/classification-rules/form";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute ClassificationRuleForm classificationRuleForm, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("formMode", "create");
			return "admin/classification-rules/form";
		}

		try {
			ruleAdminService.createRule(classificationRuleForm);
			return "redirect:/admin/classification-rules";
		}
		catch (IllegalArgumentException exception) {
			bindingResult.rejectValue("termsText", "invalid", exception.getMessage());
			model.addAttribute("formMode", "create");
			return "admin/classification-rules/form";
		}
	}

	@GetMapping("/{id}/edit")
	public String edit(@PathVariable Long id, Model model) {
		model.addAttribute("classificationRuleForm", ruleAdminService.toForm(id));
		model.addAttribute("ruleId", id);
		model.addAttribute("formMode", "edit");
		return "admin/classification-rules/form";
	}

	@PostMapping("/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute ClassificationRuleForm classificationRuleForm, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("ruleId", id);
			model.addAttribute("formMode", "edit");
			return "admin/classification-rules/form";
		}

		try {
			ruleAdminService.updateRule(id, classificationRuleForm);
			return "redirect:/admin/classification-rules";
		}
		catch (IllegalArgumentException exception) {
			bindingResult.rejectValue("termsText", "invalid", exception.getMessage());
			model.addAttribute("ruleId", id);
			model.addAttribute("formMode", "edit");
			return "admin/classification-rules/form";
		}
	}

	@PostMapping("/{id}/toggle")
	public String toggle(@PathVariable Long id) {
		ruleAdminService.toggleRule(id);
		return "redirect:/admin/classification-rules";
	}

	@PostMapping("/test")
	public String test(
			@RequestParam(defaultValue = "") String testTitle,
			@RequestParam(defaultValue = "") String testDescription,
			Model model
	) {
		// Test nutzt die gleiche Logik wie echte Tickets.
		ClassificationResult result = classificationService.classify(testTitle, testDescription);
		addRules(model);
		model.addAttribute("testTitle", testTitle);
		model.addAttribute("testDescription", testDescription);
		model.addAttribute("testResult", result);
		return "admin/classification-rules/list";
	}

	private void addRules(Model model) {
		model.addAttribute("rules", ruleAdminService.findAllRules());
	}
}
