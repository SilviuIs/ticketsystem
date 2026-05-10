package com.example.ticketsystem.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {

	private ApiDtos() {
	}

	public record ErrorResponse(String message) {
	}

	public record ReferenceResponse(Long id, String name) {

		static ReferenceResponse from(Category category) {
			if (category == null) {
				return null;
			}
			return new ReferenceResponse(category.getId(), category.getName());
		}
	}

	public record PriorityResponse(Long id, String name, int level) {

		static PriorityResponse from(Priority priority) {
			if (priority == null) {
				return null;
			}
			return new PriorityResponse(priority.getId(), priority.getName(), priority.getLevel());
		}
	}

	public record UserResponse(Long id, String username) {

		static UserResponse from(AppUser user) {
			if (user == null) {
				return null;
			}
			return new UserResponse(user.getId(), user.getUsername());
		}
	}

	public record TicketResponse(
			Long id,
			String title,
			String description,
			TicketStatus status,
			UserResponse createdBy,
			UserResponse assignedTo,
			ReferenceResponse suggestedCategory,
			ReferenceResponse finalCategory,
			PriorityResponse suggestedPriority,
			PriorityResponse finalPriority,
			Integer classificationScore,
			BigDecimal confidenceLevel,
			boolean manualReviewRequired,
			String classificationReason,
			LocalDateTime createdAt,
			LocalDateTime updatedAt
	) {

		public static TicketResponse from(Ticket ticket) {
			return new TicketResponse(
					ticket.getId(),
					ticket.getTitle(),
					ticket.getDescription(),
					ticket.getStatus(),
					UserResponse.from(ticket.getCreatedBy()),
					UserResponse.from(ticket.getAssignedTo()),
					ReferenceResponse.from(ticket.getSuggestedCategory()),
					ReferenceResponse.from(ticket.getFinalCategory()),
					PriorityResponse.from(ticket.getSuggestedPriority()),
					PriorityResponse.from(ticket.getFinalPriority()),
					ticket.getClassificationScore(),
					ticket.getConfidenceLevel(),
					ticket.isManualReviewRequired(),
					ticket.getClassificationReason(),
					ticket.getCreatedAt(),
					ticket.getUpdatedAt()
			);
		}
	}

	public record TicketDetailResponse(
			TicketResponse ticket,
			List<CommentResponse> comments,
			List<StatusHistoryResponse> history
	) {
	}

	public record CommentResponse(Long id, UserResponse author, String content, LocalDateTime createdAt) {

		public static CommentResponse from(Comment comment) {
			return new CommentResponse(
					comment.getId(),
					UserResponse.from(comment.getAuthor()),
					comment.getContent(),
					comment.getCreatedAt()
			);
		}
	}

	public record StatusHistoryResponse(
			Long id,
			TicketStatus oldStatus,
			TicketStatus newStatus,
			UserResponse changedBy,
			String note,
			LocalDateTime changedAt
	) {

		public static StatusHistoryResponse from(TicketStatusHistory history) {
			return new StatusHistoryResponse(
					history.getId(),
					history.getOldStatus(),
					history.getNewStatus(),
					UserResponse.from(history.getChangedBy()),
					history.getNote(),
					history.getChangedAt()
			);
		}
	}

	public record CreateTicketRequest(
			@NotBlank @Size(max = 255) String title,
			@NotBlank @Size(max = 4000) String description
	) {

		public TicketForm toForm() {
			TicketForm form = new TicketForm();
			form.setTitle(title);
			form.setDescription(description);
			return form;
		}
	}

	public record StatusUpdateRequest(
			@NotNull TicketStatus status,
			@Size(max = 1000) String note
	) {
	}

	public record FinalClassificationRequest(
			@NotNull Long finalCategoryId,
			@NotNull Long finalPriorityId,
			@Size(max = 1000) String note
	) {

		public FinalClassificationForm toForm() {
			FinalClassificationForm form = new FinalClassificationForm();
			form.setFinalCategoryId(finalCategoryId);
			form.setFinalPriorityId(finalPriorityId);
			form.setNote(note);
			return form;
		}
	}

	public record ClassificationPreviewRequest(
			@Size(max = 255) String title,
			@Size(max = 4000) String description
	) {
	}

	public record ClassificationPreviewResponse(
			ReferenceResponse category,
			PriorityResponse priority,
			int score,
			BigDecimal confidenceLevel,
			boolean manualReviewRequired,
			String reason
	) {

		public static ClassificationPreviewResponse from(ClassificationResult result) {
			return new ClassificationPreviewResponse(
					ReferenceResponse.from(result.category()),
					PriorityResponse.from(result.priority()),
					result.score(),
					result.confidenceLevel(),
					result.manualReviewRequired(),
					result.reason()
			);
		}
	}
}
