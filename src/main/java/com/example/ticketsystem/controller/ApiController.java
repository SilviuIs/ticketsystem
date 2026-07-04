package com.example.ticketsystem.controller;

import com.example.ticketsystem.models.ApiDtos.ClassificationPreviewRequest;
import com.example.ticketsystem.models.ApiDtos.ClassificationPreviewResponse;
import com.example.ticketsystem.models.ApiDtos.CreateTicketRequest;
import com.example.ticketsystem.models.ApiDtos.FinalClassificationRequest;
import com.example.ticketsystem.models.ApiDtos.PageResponse;
import com.example.ticketsystem.models.ApiDtos.StatusHistoryResponse;
import com.example.ticketsystem.models.ApiDtos.StatusUpdateRequest;
import com.example.ticketsystem.models.ApiDtos.TicketDetailResponse;
import com.example.ticketsystem.models.ApiDtos.TicketResponse;
import com.example.ticketsystem.models.ApiDtos.CommentResponse;
import com.example.ticketsystem.models.DashboardStatistics;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.services.ClassificationService;
import com.example.ticketsystem.services.CommentService;
import com.example.ticketsystem.services.DashboardService;
import com.example.ticketsystem.services.TicketService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

	private final TicketService ticketService;
	private final CommentService commentService;
	private final ClassificationService classificationService;
	private final DashboardService dashboardService;

	public ApiController(
			TicketService ticketService,
			CommentService commentService,
			ClassificationService classificationService,
			DashboardService dashboardService
	) {
		this.ticketService = ticketService;
		this.commentService = commentService;
		this.classificationService = classificationService;
		this.dashboardService = dashboardService;
	}

	@GetMapping("/tickets")
	public PageResponse<TicketResponse> listTickets(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size
	) {
		Page<Ticket> ticketPage = ticketService.findVisibleTickets(PageRequest.of(
				sanitizePage(page),
				sanitizePageSize(size, 20),
				Sort.by(Sort.Direction.DESC, "createdAt")
		));
		List<TicketResponse> content = ticketPage.getContent().stream()
				.map(TicketResponse::from)
				.toList();
		return PageResponse.from(ticketPage, content);
	}

	@GetMapping("/tickets/{id}")
	public TicketDetailResponse ticketDetails(@PathVariable Long id) {
		Ticket ticket = ticketService.findById(id);
		return new TicketDetailResponse(
				TicketResponse.from(ticket),
				commentService.findByTicket(ticket.getId()).stream().map(CommentResponse::from).toList(),
				ticketService.findHistory(ticket.getId()).stream().map(StatusHistoryResponse::from).toList()
		);
	}

	@PostMapping("/tickets")
	public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
		Ticket ticket = ticketService.createTicket(request.toForm());
		return ResponseEntity
				.created(URI.create("/api/tickets/" + ticket.getId()))
				.body(TicketResponse.from(ticket));
	}

	@PostMapping("/tickets/{id}/status")
	public TicketResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
		ticketService.changeStatus(id, request.status(), request.note());
		return TicketResponse.from(ticketService.findById(id));
	}

	@PostMapping("/tickets/{id}/classification")
	public TicketResponse updateFinalClassification(@PathVariable Long id, @Valid @RequestBody FinalClassificationRequest request) {
		ticketService.updateFinalClassification(id, request.toForm());
		return TicketResponse.from(ticketService.findById(id));
	}

	@PostMapping("/classification/preview")
	public ClassificationPreviewResponse previewClassification(@Valid @RequestBody ClassificationPreviewRequest request) {
		return ClassificationPreviewResponse.from(classificationService.classify(request.title(), request.description()));
	}

	@GetMapping("/dashboard")
	public DashboardStatistics dashboard() {
		return dashboardService.getStatistics();
	}

	private int sanitizePage(int page) {
		return Math.max(page, 0);
	}

	private int sanitizePageSize(int size, int defaultSize) {
		if (size < 1) {
			return defaultSize;
		}
		return Math.min(size, 100);
	}
}
