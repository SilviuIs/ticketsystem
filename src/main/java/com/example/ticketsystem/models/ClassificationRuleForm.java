package com.example.ticketsystem.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClassificationRuleForm {

	@NotBlank
	@Size(max = 255)
	private String name;

	@NotNull
	private Long categoryId;

	@NotNull
	private Long priorityId;

	@Min(0)
	private int threshold;

	private boolean active = true;

	@NotBlank
	@Size(max = 4000)
	private String termsText;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Long getPriorityId() {
		return priorityId;
	}

	public void setPriorityId(Long priorityId) {
		this.priorityId = priorityId;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getTermsText() {
		return termsText;
	}

	public void setTermsText(String termsText) {
		this.termsText = termsText;
	}
}
