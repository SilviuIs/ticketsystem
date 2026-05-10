package com.example.ticketsystem.repository;

import com.example.ticketsystem.models.Priority;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriorityRepository extends JpaRepository<Priority, Long> {

	Optional<Priority> findByName(String name);
}
