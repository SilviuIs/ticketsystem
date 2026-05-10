package com.example.ticketsystem.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "priorities")
public class Priority {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private int level;

	@Column(length = 1000)
	private String description;

	private boolean active = true;

	protected Priority() {
	}

	public Priority(String name, int level, String description) {
		this.name = name;
		this.level = level;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getLevel() {
		return level;
	}

	public String getDescription() {
		return description;
	}

	public boolean isActive() {
		return active;
	}
}
