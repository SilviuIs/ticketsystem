package com.example.ticketsystem.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.ticketsystem.models.ApiDtos.ClassificationPreviewRequest;
import com.example.ticketsystem.models.ApiDtos.ClassificationPreviewResponse;
import com.example.ticketsystem.models.ApiDtos.CreateTicketRequest;
import com.example.ticketsystem.models.ApiDtos.TicketResponse;
import com.example.ticketsystem.models.Category;
import com.example.ticketsystem.models.ClassificationResult;
import com.example.ticketsystem.models.DashboardStatistics;
import com.example.ticketsystem.models.Priority;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketForm;
import com.example.ticketsystem.services.ClassificationService;
import com.example.ticketsystem.services.CommentService;
import com.example.ticketsystem.services.DashboardService;
import com.example.ticketsystem.services.TicketService;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ApiControllerTest {

	@Mock
	private TicketService ticketService;

	@Mock
	private CommentService commentService;

	@Mock
	private ClassificationService classificationService;

	@Mock
	private DashboardService dashboardService;

	@InjectMocks
	private ApiController apiController;

	@Test
	void createTicketReturnsCreatedResponse() {
		CreateTicketRequest request = new CreateTicketRequest("VPN Problem", "Benutzer kann sich nicht verbinden");
		Ticket ticket = new Ticket();
		ReflectionTestUtils.setField(ticket, "id", 42L);
		ticket.setTitle(request.title());
		ticket.setDescription(request.description());

		when(ticketService.createTicket(any(TicketForm.class))).thenReturn(ticket);

		ResponseEntity<TicketResponse> response = apiController.createTicket(request);

		assertThat(response.getStatusCode().value()).isEqualTo(201);
		assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/tickets/42"));
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().title()).isEqualTo("VPN Problem");
	}

	@Test
	void previewClassificationReturnsJsonFriendlyDto() {
		Category category = new Category("Netzwerk", "Verbindung");
		Priority priority = new Priority("Level 1", 1, "Kritisch");
		ClassificationPreviewRequest request = new ClassificationPreviewRequest("VPN defekt", "Verbindung nicht moeglich");

		when(classificationService.classify(request.title(), request.description()))
				.thenReturn(new ClassificationResult(category, priority, 8, BigDecimal.ONE, false, "Regel erreicht"));

		ClassificationPreviewResponse response = apiController.previewClassification(request);

		assertThat(response.category().name()).isEqualTo("Netzwerk");
		assertThat(response.priority().name()).isEqualTo("Level 1");
		assertThat(response.score()).isEqualTo(8);
		assertThat(response.manualReviewRequired()).isFalse();
	}

	@Test
	void dashboardReturnsServiceStatistics() {
		DashboardStatistics statistics = new DashboardStatistics(1, 1, 0, 0, 0, 1, List.of(), List.of(), List.of());
		when(dashboardService.getStatistics()).thenReturn(statistics);

		assertThat(apiController.dashboard()).isSameAs(statistics);
	}
}
