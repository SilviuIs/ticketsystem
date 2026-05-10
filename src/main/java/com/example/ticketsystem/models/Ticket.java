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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private TicketStatus status = TicketStatus.OPEN;

	@ManyToOne(optional = false)
	@JoinColumn(name = "created_by")
	private AppUser createdBy;

	@ManyToOne
	@JoinColumn(name = "assigned_to")
	private AppUser assignedTo;

	@ManyToOne
	@JoinColumn(name = "suggested_category_id")
	private Category suggestedCategory;

	@ManyToOne
	@JoinColumn(name = "final_category_id")
	private Category finalCategory;

	@ManyToOne
	@JoinColumn(name = "suggested_priority_id")
	private Priority suggestedPriority;

	@ManyToOne
	@JoinColumn(name = "final_priority_id")
	private Priority finalPriority;

	@Column(name = "classification_score")
	private Integer classificationScore;

	@Column(name = "confidence_level", precision = 5, scale = 2)
	private BigDecimal confidenceLevel;

	@Column(name = "manual_review_required", nullable = false)
	private boolean manualReviewRequired;

	@Column(name = "classification_reason", length = 1000)
	private String classificationReason;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt = LocalDateTime.now();

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public TicketStatus getStatus() {
		return status;
	}

	public void setStatus(TicketStatus status) {
		this.status = status;
	}

	public AppUser getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(AppUser createdBy) {
		this.createdBy = createdBy;
	}

	public AppUser getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(AppUser assignedTo) {
		this.assignedTo = assignedTo;
	}

	public Category getSuggestedCategory() {
		return suggestedCategory;
	}

	public void setSuggestedCategory(Category suggestedCategory) {
		this.suggestedCategory = suggestedCategory;
	}

	public Category getFinalCategory() {
		return finalCategory;
	}

	public void setFinalCategory(Category finalCategory) {
		this.finalCategory = finalCategory;
	}

	public Priority getSuggestedPriority() {
		return suggestedPriority;
	}

	public void setSuggestedPriority(Priority suggestedPriority) {
		this.suggestedPriority = suggestedPriority;
	}

	public Priority getFinalPriority() {
		return finalPriority;
	}

	public void setFinalPriority(Priority finalPriority) {
		this.finalPriority = finalPriority;
	}

	public Integer getClassificationScore() {
		return classificationScore;
	}

	public void setClassificationScore(Integer classificationScore) {
		this.classificationScore = classificationScore;
	}

	public BigDecimal getConfidenceLevel() {
		return confidenceLevel;
	}

	public void setConfidenceLevel(BigDecimal confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}

	public boolean isManualReviewRequired() {
		return manualReviewRequired;
	}

	public void setManualReviewRequired(boolean manualReviewRequired) {
		this.manualReviewRequired = manualReviewRequired;
	}

	public String getClassificationReason() {
		return classificationReason;
	}

	public void setClassificationReason(String classificationReason) {
		this.classificationReason = classificationReason;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	@PreUpdate
	void markUpdated() {
		updatedAt = LocalDateTime.now();
	}
}
