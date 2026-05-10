package com.example.ticketsystem.repository;

import com.example.ticketsystem.models.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
