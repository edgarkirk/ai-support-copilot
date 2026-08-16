# Postmortem: Notification Delivery Outage — May 2026

## Incident Summary

**Date**: 2026-05-08 to 2026-05-09
**Duration**: 14 hours
**Severity**: S1 (Critical)
**Impact**: Approximately 12,000 transactional emails were not delivered, including order confirmations, password reset links, and payment receipts. SMS and push notifications were unaffected.

## Timeline

- **2026-05-08 16:30 UTC**: Third-party email gateway (SendGrid) began returning HTTP 503 errors for approximately 40% of API requests.
- **2026-05-08 16:45 UTC**: Notification Service retry queue began growing rapidly. Alerts triggered for queue depth exceeding threshold.
- **2026-05-08 17:00 UTC**: On-call engineer acknowledged the alert. Initial investigation pointed to the external gateway, not internal systems.
- **2026-05-08 17:30 UTC**: SendGrid published a status page update confirming degraded email delivery across their platform.
- **2026-05-08 18:00 UTC**: Support team began receiving customer complaints about missing order confirmation emails. A banner was added to the status page.
- **2026-05-09 02:00 UTC**: SendGrid resolved the issue on their end. Notification Service retry queue began draining.
- **2026-05-09 06:30 UTC**: All queued notifications were successfully delivered. Incident resolved.

## Root Cause

The outage was caused by a database migration issue at SendGrid that degraded their API availability. Our Notification Service correctly retried failed deliveries, but the retry queue grew to ~12,000 messages during the 14-hour window. The retry policy (3 attempts with exponential backoff up to 30 minutes) was insufficient for a multi-hour external outage — many notifications exhausted retries and were marked as `FAILED`.

## What Went Well

- Monitoring detected the queue depth issue within 15 minutes.
- SMS and push notification channels were unaffected, so password resets continued to work via SMS fallback.
- The status page banner reduced inbound support volume by approximately 30%.

## What Went Wrong

- The retry policy (max 3 retries over ~36 minutes) is too aggressive for extended external outages. Notifications marked as `FAILED` required manual re-sending.
- No automated mechanism to re-queue `FAILED` notifications once the external service recovered.
- Customer-facing error messaging was generic ("We're experiencing issues") rather than specific ("Email notifications are delayed, your order is confirmed").

## Action Items

1. **Extend retry policy**: Increase to 5 retries with longer backoff intervals (up to 2 hours) for external service failures. — *Completed 2026-05-20*
2. **Add bulk re-queue capability**: Admin tool to re-trigger all `FAILED` notifications within a time range. — *Completed 2026-06-10*
3. **Improve status page messaging**: Include specific affected channels and provide alternative actions customers can take. — *Completed 2026-05-15*
4. **Add secondary email provider**: Implement failover to a backup email gateway (Amazon SES) when the primary provider is unavailable. — *In progress, target Q3 2026*