package com.example.ticketsystem.repository;

import com.example.ticketsystem.models.TicketStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketStatusHistoryRepository extends JpaRepository<TicketStatusHistory, Long> {

	List<TicketStatusHistory> findByTicketIdOrderByChangedAtDesc(Long ticketId);
}
