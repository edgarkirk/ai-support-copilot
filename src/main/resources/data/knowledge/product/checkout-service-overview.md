# Checkout Service Overview

Checkout coordinates order creation, pricing confirmation, and payment authorization. It calls an external payment
gateway during the payment step.

Checkout records order state independently from the gateway. During upstream latency, an authorization can have an
uncertain state for a short period. Support tools should therefore verify authorization and capture before recommending
retry.
