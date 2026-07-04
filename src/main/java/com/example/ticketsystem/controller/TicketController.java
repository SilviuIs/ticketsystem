package com.example.ticketsystem.controller;

import com.example.ticketsystem.services.CommentService;
import com.example.ticketsystem.services.TicketService;
import com.example.ticketsystem.models.FinalClassificationForm;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.models.TicketStatus;
import com.example.ticketsystem.repository.CategoryRepository;
import com.example.ticketsystem.repository.PriorityRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TicketController {

	private final TicketService ticketService;
	private final CommentService commentService;
	private final CategoryRepository categoryRepository;
	private final PriorityRepository priorityRepository;

	public TicketController(
			TicketService ticketService,
			CommentService commentService,
			CategoryRepository categoryRepository,
			PriorityRepository priorityRepository
	) {
		this.ticketService = ticketService;
		this.commentService = commentService;
		this.categoryRepository = categoryRepository;
		this.priorityRepository = priorityRepository;
	}

	@GetMapping("/")
	public String home() {
		return "redirect:/tickets";
	}

	@GetMapping("/tickets")
	public String list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size,
			Model model
	) {
		Page<Ticket> ticketPage = ticketService.findVisibleTickets(PageRequest.of(
				sanitizePage(page),
				sanitizePageSize(size, 12),
				Sort.by(Sort.Direction.DESC, "createdAt")
		));
		model.addAttribute("ticketPage", ticketPage);
		model.addAttribute("tickets", ticketPage.getContent());
		return "tickets/list";
	}

	@GetMapping("/tickets/new")
	public String createForm(Model model) {
		model.addAttribute("ticketForm", new TicketForm());
		return "tickets/new";
	}

	@PostMapping("/tickets")
	public String create(@Valid @ModelAttribute TicketForm ticketForm, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "tickets/new";
		}
		Ticket ticket = ticketService.createTicket(ticketForm);
		return "redirect:/tickets/" + ticket.getId();
	}

	@GetMapping("/tickets/{id}")
	public String detail(@PathVariable Long id, Model model) {
		addDetailModel(ticketService.findById(id), model);
		return "tickets/detail";
	}

	@PostMapping("/tickets/{id}/classification")
	public String updateFinalClassification(
			@PathVariable Long id,
			@Valid @ModelAttribute FinalClassificationForm finalClassificationForm,
			BindingResult bindingResult,
			Model model
	) {
		if (bindingResult.hasErrors()) {
			// Bei Fehlern die Detailseite neu laden.
			addDetailModel(ticketService.findById(id), model);
			return "tickets/detail";
		}

		try {
			ticketService.updateFinalClassification(id, finalClassificationForm);
			return "redirect:/tickets/" + id;
		}
		catch (IllegalArgumentException exception) {
			bindingResult.reject("classification.invalid", exception.getMessage());
			addDetailModel(ticketService.findById(id), model);
			return "tickets/detail";
		}
	}

	@PostMapping("/tickets/{id}/status")
	public String changeStatus(@PathVariable Long id, @RequestParam TicketStatus status, @RequestParam(required = false) String note) {
		ticketService.changeStatus(id, status, note);
		return "redirect:/tickets/" + id;
	}

	@PostMapping("/tickets/{id}/comments")
	public String addComment(@PathVariable Long id, @RequestParam String content) {
		commentService.addComment(id, content);
		return "redirect:/tickets/" + id;
	}

	@PostMapping("/tickets/{id}/clarification")
	public String requestClarification(@PathVariable Long id, @RequestParam String question) {
		// Support stellt eine Rueckfrage.
		commentService.requestClarification(id, question);
		return "redirect:/tickets/" + id;
	}

	@PostMapping("/tickets/{id}/clarification-answer")
	public String answerClarification(@PathVariable Long id, @RequestParam String answer) {
		// Antwort des Benutzers auf eine Rueckfrage.
		commentService.answerClarification(id, answer);
		return "redirect:/tickets/" + id;
	}

	private void addDetailModel(Ticket ticket, Model model) {
		// Model fuer die Detailseite.
		model.addAttribute("ticket", ticket);
		model.addAttribute("comments", commentService.findByTicket(ticket.getId()));
		model.addAttribute("history", ticketService.findHistory(ticket.getId()));
		model.addAttribute("statuses", TicketStatus.values());
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("priorities", priorityRepository.findAll());
		if (!model.containsAttribute("finalClassificationForm")) {
			model.addAttribute("finalClassificationForm", toFinalClassificationForm(ticket));
		}
	}

	private FinalClassificationForm toFinalClassificationForm(Ticket ticket) {
		FinalClassificationForm form = new FinalClassificationForm();
		if (ticket.getFinalCategory() != null) {
			form.setFinalCategoryId(ticket.getFinalCategory().getId());
		}
		if (ticket.getFinalPriority() != null) {
			form.setFinalPriorityId(ticket.getFinalPriority().getId());
		}
		return form;
	}

	private int sanitizePage(int page) {
		return Math.max(page, 0);
	}

	private int sanitizePageSize(int size, int defaultSize) {
		if (size < 1) {
			return defaultSize;
		}
		return Math.min(size, 50);
	}
}
