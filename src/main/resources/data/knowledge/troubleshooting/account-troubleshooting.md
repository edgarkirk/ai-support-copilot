# Account Troubleshooting Guide

## Account Lockout

When a customer reports they cannot log in and sees a "Your account has been temporarily locked" message:

1. Confirm the lockout by checking the account status in the admin panel — look for `status: LOCKED` and the `locked_at` timestamp.
2. Determine the cause:
   - **Rate-limited lockout**: 5 or more failed login attempts within 15 minutes triggers automatic lockout. The account unlocks automatically after 30 minutes.
   - **Security lockout**: Triggered by the fraud detection system when suspicious login patterns are detected (e.g., login attempts from multiple countries within a short timeframe). These do NOT auto-unlock.
3. For rate-limited lockouts, inform the customer about the 30-minute wait period. If they need immediate access, a Tier 2 agent can manually unlock via the admin panel.
4. For security lockouts, escalate to the Security team. Do not unlock manually — the account may be compromised.

## Account Recovery

When a customer cannot access their account due to a forgotten password or lost access to their email:

1. Verify the customer's identity using at least two of the following: full name on the account, last four digits of the payment method, date of the most recent order, or the account creation date.
2. If identity is verified, initiate a password reset to the account email. If the customer no longer has access to that email, escalate to Tier 2 with the identity verification evidence.
3. Tier 2 can update the account email after additional verification (government ID via secure upload portal). This process takes 24-48 hours.

## Profile Update Issues

Common issues when customers try to update their profile:

- **Email change not saving**: The new email must be verified via confirmation link before the change takes effect. The link expires after 24 hours. If expired, the customer must re-initiate the email change.
- **Name change rejected**: Names containing special characters or exceeding 100 characters are rejected by validation. Inform the customer of these constraints.
- **Address update not reflected in orders**: Address changes only apply to future orders. Existing orders retain the address at the time of placement.

## Account Deletion Requests

Per GDPR and company policy:

1. Confirm the customer understands that deletion is permanent and includes all order history, saved payment methods, and preferences.
2. Check for active subscriptions or pending orders — these must be resolved before deletion.
3. Submit the deletion request via the admin panel. Processing takes up to 30 days as required by the data retention policy.
4. The customer will receive a confirmation email when deletion is complete.