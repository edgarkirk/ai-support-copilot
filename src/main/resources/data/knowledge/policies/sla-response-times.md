# SLA Response Times

## Severity Levels and Response Targets

| Severity | First Response | Resolution Target | Examples |
|----------|---------------|-------------------|----------|
| Critical (S1) | 15 minutes | 4 hours | Complete service outage, data breach, payment processing down for all users |
| High (S2) | 1 hour | 8 hours | Feature-wide failure affecting >10% of users, security vulnerability, payment failures for a subset of users |
| Medium (S3) | 4 hours | 48 hours | Intermittent errors, degraded performance, individual account issues requiring investigation |
| Low (S4) | 24 hours | 5 business days | Feature requests, cosmetic issues, documentation questions, general inquiries |

## Team Assignments

- **Tier 1 (Frontline Support)**: Handles S3 and S4 tickets. Performs initial triage for all incoming tickets.
- **Tier 2 (Senior Support)**: Handles S2 tickets and escalations from Tier 1. Has admin panel access for account modifications.
- **Tier 3 (Engineering Support)**: Handles S1 tickets and complex technical issues. On-call rotation with 24/7 coverage.
- **Security Team**: All security-related incidents regardless of severity. Separate escalation path.

## Escalation Rules

- If a ticket is not acknowledged within the first response target, it auto-escalates to the next tier.
- If a ticket is not resolved within the resolution target, the team lead is notified and a review is triggered.
- Customers with Enterprise plans receive S2 response times for all tickets regardless of actual severity.
- During declared incidents (posted on the status page), individual tickets related to the incident are linked to the incident ticket and do not count against SLA metrics individually.

## Business Hours

- Standard support: Monday-Friday, 08:00-20:00 UTC
- Critical (S1) support: 24/7/365
- Holiday coverage: S1 and S2 only, with reduced staffing