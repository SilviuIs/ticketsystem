package com.example.ticketsystem.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "classification_terms")
public class ClassificationTerm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "rule_id")
	private ClassificationRule rule;

	@Column(nullable = false)
	private String term;

	@Column(nullable = false)
	private int weight;

	private boolean active = true;

	protected ClassificationTerm() {
	}

	public ClassificationTerm(ClassificationRule rule, String term, int weight) {
		this.rule = rule;
		this.term = term;
		this.weight = weight;
	}

	public Long getId() {
		return id;
	}

	public ClassificationRule getRule() {
		return rule;
	}

	public String getTerm() {
		return term;
	}

	public int getWeight() {
		return weight;
	}

	public boolean isActive() {
		return active;
	}
}
