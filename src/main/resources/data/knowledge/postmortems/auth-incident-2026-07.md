# Postmortem: Authentication Capacity Degradation

Incident: INC-2026-0710
Date: 2026-07-10

## Summary

Authentication latency increased for 38 minutes because one service pool reached connection limits.

## Symptoms

Users reported slow login and occasional HTTP 503 responses. Password reset was not a common factor and HTTP 401 was not
the dominant error.

## Root cause

Database connection exhaustion in the Authentication service.

## Resolution

Connection pool configuration was adjusted and traffic normalized.

## Distinguishing signal

This incident should not be confused with the March stale-session incident. July capacity degradation primarily produced
latency and HTTP 503, while the stale-session problem produced HTTP 401 after credential changes.
