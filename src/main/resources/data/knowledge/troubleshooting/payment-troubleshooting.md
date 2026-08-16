# Payment Troubleshooting Guide

## Checkout timeout symptoms

Customers may report an endless payment spinner, gateway timeout, HTTP 504, or a payment request that fails before
confirmation.

## First checks

Verify whether the order was created and whether payment authorization or capture exists. Never tell a customer to retry
until capture status is known, because an uncertain capture can create duplicate-payment risk.

## Known latency pattern

A payment gateway under high latency may cause authorization requests to exceed the Checkout service timeout. In this
case the customer may see a failure even when the upstream gateway continues processing.

## Recommended handling

1. Check payment status.
2. If no authorization or capture exists, retry only after gateway health is normal.
3. If status is uncertain, escalate to Payments.
4. If a duplicate capture is detected, mark Critical and follow the financial incident process.
