---
document_id: PHASE-17.6I
title: Add To Cart Button Visibility Check — Missed Scroll Site
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6A, PHASE-17.6B, PHASE-17.6H]
classification: Internal
---

# Phase 17.6I — Add To Cart Button Visibility Check: Missed Scroll Site

| Field | Value |
|---|---|
| Failed Workflow Run | [31209423230](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31209423230) |
| Result | 19 tests completed, 2 failed — `accessDrawerItems` (known, undisputed) and `addProductToCart`. **Phase 17.6H's `getCardPrice` fix fully succeeded** — the test progressed past product card verification, navigation, and Product Details identity checks, further than any prior run. |

---

## Executive Summary

`addProductToCart` (TC-012) now fails at `CommonAssertions.verifyVisible(productDetailsPage.isAddToCartButtonDisplayed(), "Add To Cart button")` (`ProductDetailsTest.java:90`) — a **different** assertion than any prior finding in this phase, reached only because Phase 17.6H's fix worked.

**Root cause:** Phase 17.6A fixed `ProductDetailsPage.addToCart()` (the click action) by scrolling to `ADD_TO_CART_BUTTON` first, but did not add the same scroll to `isAddToCartButtonDisplayed()` — a separate method checking the same element's visibility, called by TC-012 (`addProductToCart`) *before* `addToCart()` is ever invoked. `CartTest`'s shared helper (fixed successfully in Phase 17.6B) never calls `isAddToCartButtonDisplayed()` — it goes straight to `addToCart()` — which is why that fix was sufficient there but not here. This is a gap in Phase 17.6A's coverage, not a new defect class.

**Classification: Synchronization issue** — identical mechanism to Phase 17.6A, applied to a call site Phase 17.6A did not reach.

---

## Exact Command / Error

```
java.lang.AssertionError: verifyVisible [Add To Cart button]: expected to be visible
    at com.mobileautomation.framework.assertions.CommonAssertions.evaluate(CommonAssertions.java:83)
    at com.mobileautomation.framework.tests.ProductDetailsTest.addProductToCart(ProductDetailsTest.java:90)
```

Preceded by full success through product card verification, navigation, and Product Details identity checks (all previously-blocked assertions now passing):

```
19:11:36.343  Assertion passed: verifyText [Product Card — Product Name]
19:11:36.343  Assertion passed: verifyText [Product Card — Product Price]
19:11:38.025  Assertion passed: verifyVisible [Product Details screen]
19:11:38.495  Assertion passed: verifyText [Product Details — Product Name]
19:11:38.543  Assertion passed: verifyText [Product Details — Product Price]
19:11:53.566  Assertion failed: verifyVisible [Add To Cart button]
```

---

## Root Cause

`ProductDetailsPage.isAddToCartButtonDisplayed()` (line 65–68) checks `ADD_TO_CART_BUTTON` without scrolling, unlike `addToCart()` (line 59–63, fixed in Phase 17.6A), which scrolls to the same locator before clicking it. `ProductDetailsTest.addProductToCart` (TC-012) calls `isAddToCartButtonDisplayed()` first (line 90), before ever reaching `addToCart()` — so the missing scroll in this one method blocks the test before the already-fixed method is ever reached.

---

## Confidence

**High** — direct evidence from a run where every other synchronization fix in this phase is now confirmed working, isolating this as the one remaining gap in that specific fix's coverage.

---

## Proposed Fix

*(Described only — implemented in the accompanying Phase 17.6J report.)*

Add the same `ScrollUtility.scrollToAccessibilityId(ProductDetailsLocators.ADD_TO_CART_BUTTON_ACCESSIBILITY_ID)` call already used in `addToCart()` to `isAddToCartButtonDisplayed()`, before its existing visibility check.

---

**End of Document — Phase 17.6I Root Cause Report, v1.0**
