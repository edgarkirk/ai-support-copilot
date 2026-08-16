# Postmortem: Authentication Failures After Password Reset

Incident: INC-2026-0318
Date: 2026-03-18

## Summary

Customers who successfully changed their passwords intermittently received HTTP 401 when starting or continuing
sessions. Mobile users were affected more frequently, although Web Portal reports also occurred.

## Impact

Approximately 240 authentication attempts were affected during the incident window. Password reset itself remained
available.

## Root cause

A deployment changed session-cache invalidation behavior. Credential updates were committed correctly, but some cached
session metadata was not invalidated. Clients could therefore present a session state associated with the previous
credentials.

## Detection

Support identified a cluster of tickets containing "password reset", "401", and "login failed". Monitoring showed no
increase in password-reset failures, which initially made the incident harder to identify.

## Mitigation

Operations flushed affected session-cache entries. Support instructed users to sign out from all sessions and perform a
fresh login.

## Follow-up

A cache-invalidation fix was deployed. A known-issue entry was created because similar symptoms should be investigated
quickly if they recur.
