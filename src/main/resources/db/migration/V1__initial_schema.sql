CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ticket
(
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMP   NOT NULL,
    customer_id VARCHAR(100),
    product     VARCHAR(100),
    category    VARCHAR(100),
    priority    VARCHAR(20) NOT NULL,
    status      VARCHAR(30) NOT NULL,
    description TEXT        NOT NULL,
    resolution  TEXT,
    resolved_at TIMESTAMP
);

CREATE INDEX idx_tickets_created_at
    ON ticket (created_at);

CREATE INDEX idx_tickets_category
    ON ticket (category);

CREATE INDEX idx_tickets_priority
    ON ticket (priority);