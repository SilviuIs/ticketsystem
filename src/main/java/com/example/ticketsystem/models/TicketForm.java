package com.example.ticketsystem.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TicketForm {

	@NotBlank
	@Size(max = 255)
	private String title;

	@NotBlank
	@Size(max = 4000)
	private String description;

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
}
