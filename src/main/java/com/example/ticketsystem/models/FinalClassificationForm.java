package com.example.ticketsystem.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FinalClassificationForm {

	@NotNull
	private Long finalCategoryId;

	@NotNull
	private Long finalPriorityId;

	@Size(max = 1000)
	private String note;

	public Long getFinalCategoryId() {
		return finalCategoryId;
	}

	public void setFinalCategoryId(Long finalCategoryId) {
		this.finalCategoryId = finalCategoryId;
	}

	public Long getFinalPriorityId() {
		return finalPriorityId;
	}

	public void setFinalPriorityId(Long finalPriorityId) {
		this.finalPriorityId = finalPriorityId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
