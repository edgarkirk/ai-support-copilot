# Postmortem: Checkout Payment Timeouts

Incident: INC-2026-0727
Date: 2026-07-27

## Summary

Checkout experienced a sharp increase in payment timeouts between July 24 and July 31, with the highest impact on July
27.

## Customer symptoms

Customers reported an endless payment spinner, HTTP 504, gateway timeout messages, and uncertainty about whether payment
completed.

## Root cause

The external payment gateway experienced sustained latency. Checkout's timeout was shorter than some gateway
authorization processing times, so requests could terminate before a final gateway response arrived.

## Risk

Asking customers to retry without checking capture status could create duplicate-payment risk.

## Mitigation

Support checked authorization/capture state before retry. Payments engineering adjusted timeout handling and worked with
the provider.

## Resolution

Gateway latency returned to normal and timeout rates dropped.
