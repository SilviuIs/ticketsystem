CREATE DATABASE IF NOT EXISTS ticketsystem
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ticketsystem;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS priorities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    level INT NOT NULL,
    description VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_priorities_name UNIQUE (name),
    CONSTRAINT uk_priorities_level UNIQUE (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS classification_rules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    priority_id BIGINT NOT NULL,
    threshold INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_classification_rules_category (category_id),
    INDEX idx_classification_rules_priority (priority_id),
    CONSTRAINT fk_classification_rules_category
        FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_classification_rules_priority
        FOREIGN KEY (priority_id) REFERENCES priorities (id),
    CONSTRAINT chk_classification_rules_threshold CHECK (threshold >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS classification_terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rule_id BIGINT NOT NULL,
    term VARCHAR(255) NOT NULL,
    weight INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    INDEX idx_classification_terms_rule (rule_id),
    CONSTRAINT uk_classification_terms_rule_term UNIQUE (rule_id, term),
    CONSTRAINT fk_classification_terms_rule
        FOREIGN KEY (rule_id) REFERENCES classification_rules (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_classification_terms_weight CHECK (weight > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tickets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by BIGINT NOT NULL,
    assigned_to BIGINT,
    suggested_category_id BIGINT,
    final_category_id BIGINT,
    suggested_priority_id BIGINT,
    final_priority_id BIGINT,
    classification_score INT,
    confidence_level DECIMAL(5, 2),
    manual_review_required BOOLEAN NOT NULL DEFAULT FALSE,
    classification_reason VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_tickets_status (status),
    INDEX idx_tickets_created_by (created_by),
    INDEX idx_tickets_assigned_to (assigned_to),
    INDEX idx_tickets_suggested_category (suggested_category_id),
    INDEX idx_tickets_final_category (final_category_id),
    INDEX idx_tickets_suggested_priority (suggested_priority_id),
    INDEX idx_tickets_final_priority (final_priority_id),
    CONSTRAINT fk_tickets_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_tickets_assigned_to
        FOREIGN KEY (assigned_to) REFERENCES users (id),
    CONSTRAINT fk_tickets_suggested_category
        FOREIGN KEY (suggested_category_id) REFERENCES categories (id),
    CONSTRAINT fk_tickets_final_category
        FOREIGN KEY (final_category_id) REFERENCES categories (id),
    CONSTRAINT fk_tickets_suggested_priority
        FOREIGN KEY (suggested_priority_id) REFERENCES priorities (id),
    CONSTRAINT fk_tickets_final_priority
        FOREIGN KEY (final_priority_id) REFERENCES priorities (id),
    CONSTRAINT chk_tickets_confidence_level CHECK (confidence_level IS NULL OR (confidence_level >= 0 AND confidence_level <= 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_comments_ticket (ticket_id),
    INDEX idx_comments_author (author_id),
    CONSTRAINT fk_comments_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_author
        FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS ticket_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    ticket_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL,
    note VARCHAR(1000),
    changed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_ticket_status_history_ticket (ticket_id),
    INDEX idx_ticket_status_history_changed_by (changed_by),
    CONSTRAINT fk_ticket_status_history_ticket
        FOREIGN KEY (ticket_id) REFERENCES tickets (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ticket_status_history_changed_by
        FOREIGN KEY (changed_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
