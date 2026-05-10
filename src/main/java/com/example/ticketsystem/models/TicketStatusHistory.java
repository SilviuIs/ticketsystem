package com.example.ticketsystem.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_status_history")
public class TicketStatusHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "ticket_id")
	private Ticket ticket;

	@Enumerated(EnumType.STRING)
	@Column(name = "old_status", length = 50)
	private TicketStatus oldStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, length = 50)
	private TicketStatus newStatus;

	@ManyToOne(optional = false)
	@JoinColumn(name = "changed_by")
	private AppUser changedBy;

	@Column(length = 1000)
	private String note;

	@Column(name = "changed_at", nullable = false)
	private LocalDateTime changedAt = LocalDateTime.now();

	protected TicketStatusHistory() {
	}

	public TicketStatusHistory(Ticket ticket, TicketStatus oldStatus, TicketStatus newStatus, AppUser changedBy, String note) {
		this.ticket = ticket;
		this.oldStatus = oldStatus;
		this.newStatus = newStatus;
		this.changedBy = changedBy;
		this.note = note;
	}

	public Long getId() {
		return id;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public TicketStatus getOldStatus() {
		return oldStatus;
	}

	public TicketStatus getNewStatus() {
		return newStatus;
	}

	public AppUser getChangedBy() {
		return changedBy;
	}

	public String getNote() {
		return note;
	}

	public LocalDateTime getChangedAt() {
		return changedAt;
	}
}
