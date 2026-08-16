# Authentication Troubleshooting Guide

## Common symptoms

Authentication issues may appear as HTTP 401, repeated login prompts, an apparently successful password reset followed
by login failure, or different behavior between web and mobile clients.

## Password-reset workflow

1. Confirm that the password reset completed successfully.
2. Ask the user to sign out of all active sessions.
3. Start a new session and authenticate with the new password.
4. Check whether the account is temporarily locked.
5. Record whether the failure occurs on Web Portal, Mobile App, or both.

## Stale session symptoms

A stale session may continue to present old authentication state after credentials change. Typical reports include "
password reset worked but login is unauthorized" and "web works but mobile still returns 401."

If a fresh login succeeds after all sessions are invalidated, no further escalation is required.

## Escalation

If HTTP 401 persists after a confirmed reset, session invalidation, and fresh login, escalate to Authentication. Include
timestamps and representative symptoms. Do not recommend repeated password resets as a general workaround.
