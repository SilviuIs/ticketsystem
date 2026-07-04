package com.example.ticketsystem.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticketsystem.models.AuditEvent;
import com.example.ticketsystem.repository.AuditEventRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditServiceTest {

	@Autowired
	private AuditService auditService;

	@Autowired
	private AuditEventRepository auditEventRepository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void recordPersistsAuditEvent() {
		auditService.record(
				"TICKET_STATUS_CHANGED",
				"support1",
				"Ticket",
				42L,
				"Status changed from OPEN to IN_PROGRESS"
		);

		entityManager.flush();
		entityManager.clear();

		assertThat(auditEventRepository.findAll())
				.singleElement()
				.satisfies(event -> {
					assertThat(event.getEventType()).isEqualTo("TICKET_STATUS_CHANGED");
					assertThat(event.getActorUsername()).isEqualTo("support1");
					assertThat(event.getEntityType()).isEqualTo("Ticket");
					assertThat(event.getEntityId()).isEqualTo(42L);
					assertThat(event.getSummary()).isEqualTo("Status changed from OPEN to IN_PROGRESS");
					assertThat(event.getCreatedAt()).isNotNull();
				});
	}
}
