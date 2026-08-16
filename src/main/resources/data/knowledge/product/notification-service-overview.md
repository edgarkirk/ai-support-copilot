# Notification Service Overview

## Purpose

The Notification Service is responsible for delivering transactional communications to customers across multiple channels: email, SMS, and in-app push notifications. It acts as a centralized gateway — other services publish notification events, and the Notification Service handles delivery, templating, and retry logic.

## Architecture

The service consumes events from a message queue (RabbitMQ). Each event contains:
- `recipient_id` — the customer's account ID
- `event_type` — determines which template to use (e.g., `ORDER_CONFIRMED`, `PASSWORD_RESET`, `PAYMENT_FAILED`)
- `channel` — preferred delivery channel (`EMAIL`, `SMS`, `PUSH`), with fallback rules
- `payload` — dynamic data to populate the template (order details, amounts, links)

## Delivery Channels

- **Email**: Primary channel for order confirmations, receipts, and account notifications. Delivered via a third-party email gateway (SendGrid). Subject to external rate limits (500/min on current plan).
- **SMS**: Used for password resets (as fallback), two-factor authentication codes, and shipping alerts. Delivered via Twilio. Higher cost per message — only used when explicitly requested or as email fallback.
- **Push notifications**: In-app notifications for order status updates and promotions. Delivered via Firebase Cloud Messaging (FCM). Only works if the customer has the mobile app installed and notifications enabled.

## Retry Policy

Failed deliveries are retried with exponential backoff:
- 1st retry: 1 minute after failure
- 2nd retry: 5 minutes
- 3rd retry: 30 minutes
- After 3 failed retries, the notification is marked as `FAILED` and an alert is sent to the operations team.

## Deduplication

To prevent duplicate notifications (see KI-NOTIF-03), the service maintains a deduplication window. Events with the same `recipient_id` + `event_type` combination within a 5-minute window are treated as duplicates and only the first is processed.

## Customer Preferences

Customers can configure notification preferences in their account settings:
- Opt out of marketing/promotional notifications
- Choose preferred channel for transactional notifications (email vs. SMS)
- Transactional notifications (password resets, payment confirmations) cannot be disabled — these are required for account security and compliance.