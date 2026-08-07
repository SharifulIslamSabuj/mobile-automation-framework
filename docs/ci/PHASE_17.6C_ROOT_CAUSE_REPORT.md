---
document_id: PHASE-17.6C
title: To Payment Button Synchronization Root Cause Analysis
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6A, PHASE-17.6B]
classification: Internal
---

# Phase 17.6C — To Payment Button Synchronization Root Cause Analysis

| Field | Value |
|---|---|
| Failed Workflow Run | [31196174453](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31196174453) |
| Result of Prior Fix (17.6B) | Confirmed — 19 tests completed, only 6 failed (down from 16). All 13 tests targeted by Phase 17.6B now pass. |
| Tests Affected By This Finding | `CartTest.proceedToPayment` (TC-021), `accessPaymentScreen` (TC-022), `enterPaymentCardData` (TC-023), `placeOrder` (TC-026) — 4 of the 6 remaining failures |

---

## Executive Summary

Phase 17.6B's fix is confirmed working by direct evidence: the CartTest/TC-010/TC-011 failures it targeted are gone. Of the 6 failures remaining in this run, 4 share one new, single root cause — the same defect class as Phase 17.6A (a control below the fold, not scrolled into view), now on the Checkout — Shipping screen instead of Product Details.

All four failing tests call `checkoutPage.enterShippingAddress(address)` then immediately `CommonAssertions.verifyVisible(checkoutPage.isToPaymentButtonDisplayed(), "To Payment button")`, and all four fail at that exact assertion. The failure screenshot (`proceedToPayment_failure_20260807_161826_cs5SF5.png`) shows the shipping form fully and correctly populated (Full Name, Address Line 1/2, City, State/Region, Zip Code, Country all filled) — but the "To Payment" button is not present in the visible viewport, cut off below the last field.

**Classification: Synchronization issue** — identical in kind to Phase 17.6A, at a different screen.

---

## Exact Command / Error

```
java.lang.AssertionError at CartTest.java:407   (proceedToPayment, TC-021)
java.lang.AssertionError at CartTest.java:445   (accessPaymentScreen, TC-022)
java.lang.AssertionError at CartTest.java:505   (enterPaymentCardData, TC-023)
java.lang.AssertionError at CartTest.java:569   (placeOrder, TC-026)
```

All four are the same source line pattern:

```java
CommonAssertions.verifyVisible(checkoutPage.isToPaymentButtonDisplayed(), "To Payment button");
```

---

## Root Cause

`CheckoutPage.isToPaymentButtonDisplayed()` checks `CheckoutLocators.TO_PAYMENT_BUTTON` (`AppiumBy.accessibilityId("Saves user info for checkout")`) without first scrolling it into view. On this CI emulator's viewport, after the shipping form's six fields are populated, the "To Payment" button sits below the last visible field — confirmed directly by the failure screenshot, not inferred.

This is the same defect class Phase 17.6A already found and fixed in `ProductDetailsPage` (missing scroll-into-view before interacting with a below-the-fold control), occurring independently in `CheckoutPage`. `CheckoutPage` has no existing scroll-to-element precedent of its own (unlike `ProductDetailsPage`'s `scrollToHighlights()`), but the same `ScrollUtility.scrollToAccessibilityId(...)` mechanism Phase 17.6B used applies directly here with no new logic required.

---

## Evidence

Failure screenshot `proceedToPayment_failure_20260807_161826_cs5SF5.png` shows the Checkout screen with all shipping fields correctly filled and no "To Payment" button visible in the viewport — direct visual confirmation, matching the exact pattern Phase 17.6A's screenshot evidence established for the Product Details screen.

`CheckoutPage.java` (current state, lines 180–188) confirms no scroll call precedes the `TO_PAYMENT_BUTTON` visibility/enabled checks.

---

## Confidence

**High** — same evidentiary basis as Phase 17.6A (direct screenshot showing the target element absent from the rendered viewport), and the fix mechanism is already proven correct by Phase 17.6B's confirmed result.

---

## Proposed Fix

*(Described only — implemented in the accompanying Phase 17.6D report.)*

Expose `TO_PAYMENT_BUTTON`'s raw accessibility id in `CheckoutLocators` (same pattern as Phase 17.6B), and call `ScrollUtility.scrollToAccessibilityId(...)` at the start of `CheckoutPage.isToPaymentButtonDisplayed()`, before the existing visibility check.

---

**End of Document — Phase 17.6C Root Cause Report, v1.0**
