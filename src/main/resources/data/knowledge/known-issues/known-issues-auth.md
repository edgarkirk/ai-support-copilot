# Authentication Known Issues

## KI-AUTH-17 — Stale session after credential change

Status: Monitoring
First observed: 2026-03-18

Some sessions can retain stale authentication metadata after a password change. The most recognizable symptom is HTTP
401 after a successful reset. Affected users may report different results between Web Portal and Mobile App.

Workaround: invalidate all active sessions and authenticate again. Persistent failures must be escalated.

## KI-AUTH-09 — Temporary lock after repeated failures

Status: Known behavior
Five failed attempts within 15 minutes can temporarily lock an account.
