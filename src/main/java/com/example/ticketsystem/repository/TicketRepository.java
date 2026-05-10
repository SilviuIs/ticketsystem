package com.example.ticketsystem.repository;

import com.example.ticketsystem.models.StatisticItem;
import com.example.ticketsystem.models.Ticket;
import com.example.ticketsystem.models.TicketStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

	@Override
	@EntityGraph(attributePaths = {"createdBy", "assignedTo", "suggestedCategory", "suggestedPriority", "finalCategory", "finalPriority"})
	List<Ticket> findAll();

	@EntityGraph(attributePaths = {"createdBy", "assignedTo", "suggestedCategory", "suggestedPriority", "finalCategory", "finalPriority"})
	List<Ticket> findByCreatedByUsername(String username);

	long countByStatus(TicketStatus status);

	long countByStatusIn(Collection<TicketStatus> statuses);

	long countByManualReviewRequiredTrue();

	long countByAssignedToIsNull();

	@Query("""
			select new com.example.ticketsystem.models.StatisticItem(coalesce(category.name, 'Keine Kategorie'), count(ticket))
			from Ticket ticket
			left join ticket.finalCategory category
			group by category.name
			order by count(ticket) desc
			""")
	List<StatisticItem> countByFinalCategory();

	@Query("""
			select new com.example.ticketsystem.models.StatisticItem(coalesce(priority.name, 'Keine Prioritaet'), count(ticket))
			from Ticket ticket
			left join ticket.finalPriority priority
			group by priority.name, priority.level
			order by priority.level asc
			""")
	List<StatisticItem> countByFinalPriority();
}
