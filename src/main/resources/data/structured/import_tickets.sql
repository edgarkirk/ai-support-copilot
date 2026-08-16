-- Run with psql from the directory containing tickets.csv
-- Example:
-- psql -h localhost -U ai_support -d ai_support -f import_tickets.sql

COPY ticket(id, created_at, customer_id, product, category, priority, status, description, resolution, resolved_at) FROM '/tmp/tickets.csv' WITH (FORMAT csv, HEADER true, NULL '');

SELECT setval(pg_get_serial_sequence('ticket', 'id'), COALESCE(MAX(id), 1))
FROM ticket;
