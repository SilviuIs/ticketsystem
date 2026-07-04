package com.example.ticketsystem.repository;

import com.example.ticketsystem.models.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
}
