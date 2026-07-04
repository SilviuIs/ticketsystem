CREATE TABLE IF NOT EXISTS audit_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    event_type VARCHAR(100) NOT NULL,
    actor_username VARCHAR(255),
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    summary VARCHAR(1000) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_events_created_at (created_at),
    INDEX idx_audit_events_event_type (event_type),
    INDEX idx_audit_events_entity (entity_type, entity_id),
    INDEX idx_audit_events_actor (actor_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
