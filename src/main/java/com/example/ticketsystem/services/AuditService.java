package com.example.ticketsystem.services;

import com.example.ticketsystem.models.AuditEvent;
import com.example.ticketsystem.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

	private final AuditEventRepository auditEventRepository;

	public AuditService(AuditEventRepository auditEventRepository) {
		this.auditEventRepository = auditEventRepository;
	}

	@Transactional
	public void record(String eventType, String actorUsername, String entityType, Long entityId, String summary) {
		auditEventRepository.save(new AuditEvent(eventType, actorUsername, entityType, entityId, summary));
	}
}
