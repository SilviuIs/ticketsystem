package com.example.ticketsystem.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(name = "actor_username", length = 255)
	private String actorUsername;

	@Column(name = "entity_type", nullable = false, length = 100)
	private String entityType;

	@Column(name = "entity_id")
	private Long entityId;

	@Column(nullable = false, length = 1000)
	private String summary;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	protected AuditEvent() {
	}

	public AuditEvent(String eventType, String actorUsername, String entityType, Long entityId, String summary) {
		this.eventType = eventType;
		this.actorUsername = actorUsername;
		this.entityType = entityType;
		this.entityId = entityId;
		this.summary = summary;
	}

	public Long getId() {
		return id;
	}

	public String getEventType() {
		return eventType;
	}

	public String getActorUsername() {
		return actorUsername;
	}

	public String getEntityType() {
		return entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public String getSummary() {
		return summary;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
