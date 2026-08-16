# Known Issues — Notifications

## KI-NOTIF-01: Delayed Order Confirmation Emails

**Status**: Active
**First reported**: 2026-04-12
**Affected systems**: Notification Service, Email Gateway

Order confirmation emails are intermittently delayed by 15-60 minutes during peak hours (10:00-14:00 UTC). Root cause is rate limiting by the third-party email provider when send volume exceeds 500 emails per minute.

**Workaround**: Customers can verify their order status in the app or website under "My Orders" without waiting for the email. Support agents should check the notification delivery log in the admin panel — if the status shows `QUEUED`, the email will arrive eventually. If the status shows `FAILED`, re-trigger the notification manually.

**Fix timeline**: Engineering is evaluating migration to a higher-tier email provider plan. Expected resolution by Q4 2026.

## KI-NOTIF-02: Password Reset Emails Landing in Spam

**Status**: Active
**First reported**: 2026-02-20
**Affected systems**: Notification Service

Some email providers (particularly Yahoo and Outlook) are flagging password reset emails as spam. This is due to a DKIM configuration issue on the `noreply@` subdomain that was introduced during the January 2026 email infrastructure migration.

**Workaround**: Advise customers to check their spam/junk folder and mark the email as "Not Spam." If the customer cannot find the email at all, a Tier 2 agent can initiate an alternative reset via SMS verification (if the customer has a phone number on file).

**Fix timeline**: The infrastructure team has scheduled a DKIM reconfiguration for September 2026.

## KI-NOTIF-03: Duplicate Shipping Notifications (Resolved)

**Status**: Resolved (2026-06-01)
**First reported**: 2026-05-15
**Affected systems**: Notification Service, Order Service

Customers were receiving 2-3 duplicate "Your order has shipped" emails for a single shipment. The issue was caused by a retry loop in the Order Service webhook that sent duplicate events to the Notification Service when the initial acknowledgment was delayed.

**Resolution**: A deduplication check was added to the Notification Service based on `order_id` + `event_type` with a 5-minute window. Deployed on 2026-06-01.