package com.example.ticketsystem.repository;

import com.example.ticketsystem.models.ClassificationRule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClassificationRuleRepository extends JpaRepository<ClassificationRule, Long> {

	boolean existsByName(String name);

	@EntityGraph(attributePaths = {"category", "priority", "terms"})
	List<ClassificationRule> findByActiveTrue();

	@Query("""
			select distinct rule
			from ClassificationRule rule
			left join fetch rule.category
			left join fetch rule.priority
			left join fetch rule.terms
			order by rule.name
			""")
	List<ClassificationRule> findAllWithDetails();

	@Query("""
			select rule
			from ClassificationRule rule
			left join fetch rule.category
			left join fetch rule.priority
			left join fetch rule.terms
			where rule.id = :id
			""")
	Optional<ClassificationRule> findByIdWithDetails(Long id);
}
