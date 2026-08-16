# Support Escalation Policy

Version: 4.1

## Severity guidance

Critical: widespread outage, security exposure, duplicate financial capture, or complete inability to use a critical
service.
High: many customers affected, recurring failures, or a workaround is unreliable.
Medium: limited customer impact with a usable workaround.
Low: informational requests or cosmetic problems.

## Authentication escalation

Escalate to the Authentication team when HTTP 401 continues after a confirmed password reset, stale sessions have been
cleared, and the user has attempted a fresh login.

## Payments escalation

Escalate repeated gateway timeouts, HTTP 504 responses, or cases where payment capture status cannot be established.
Potential duplicate charges must be treated as Critical.

## Evidence required

Include timestamps, affected product, representative ticket IDs, error symptoms, attempted workaround, and any relevant
incident reference.
