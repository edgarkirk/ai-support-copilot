# Payments Known Issues

## KI-PAY-31 — Gateway latency and Checkout 504

Status: Monitoring

High upstream gateway latency can cause Checkout authorization requests to exceed timeout limits. Symptoms include
payment spinner, HTTP 504, and missing confirmation.

Before retrying, confirm whether authorization or capture completed.

## KI-PAY-22 — Duplicate button submission

Status: Resolved
A previous UI issue allowed rapid repeated Pay clicks. This issue was fixed in February 2026 and is not associated with
gateway HTTP 504 errors.
