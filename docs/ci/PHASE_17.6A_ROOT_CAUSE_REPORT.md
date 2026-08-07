---
document_id: PHASE-17.6A
title: Cart/Quantity/Color Selector Synchronization Root Cause Analysis
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.5G]
classification: Internal
---

# Phase 17.6A — Cart/Quantity/Color Selector Synchronization Root Cause Analysis

| Field | Value |
|---|---|
| Failed Workflow Run | [31174085507](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31174085507) |
| Failed Step | `Android Emulator Provisioning, Build Validation & Test Execution` — Gradle `test` task |
| Tests Affected | 13 of 16 failures: all 11 `CartTest` methods (via a shared setup helper), plus `ProductDetailsTest.adjustProductQuantity` (TC-011) and `ProductDetailsTest.selectProductColor` (TC-010) |

---

## Executive Summary

Phase 17.5G's initial pattern-match (grep across the full log) conflated several distinct failures under a single assumed cause. A precise, per-test extraction of each failure's exact exception and stack frame (this report's own evidence-gathering) shows 13 of the 16 failing tests share one genuine, single root cause, distinct from the other 3 failures (TC-012, TC-028), which are **not** addressed by this report.

**Root cause:** `ProductDetailsPage.addToCart()`, `isQuantitySelectorDisplayed()`, and `isColorSelectorDisplayed()` interact with elements (the Add to Cart button, Quantity controls, Color Selector) without first scrolling them into view. On this CI emulator's viewport (`1080x1920`, `420dpi`, `pixelRatio: 2.625`, usable viewport height `1857px` per the Appium session capabilities), these elements render below the fold on initial screen load. The real-device baseline this framework was built and verified against apparently does not require this scroll (different screen dimensions), so the omission was never exercised as a defect until this CI run.

This is a **Synchronization issue** — a missing scroll-into-view step, not a locator error, not a timing/wait-duration problem, and not an emulator limitation that must simply be documented. It is fixable without touching real-device behavior, per Step 5.

---

## Exact Command / Error

**Cluster (11 tests: `accessCartScreen`, `accessPaymentScreen`, `accessShippingScreen`, `enterPaymentCardData`, `enterShippingAddressData`, `placeOrder`, `proceedToCheckoutAnonymousUser`, `proceedToCheckoutAuthenticatedUser`, `proceedToPayment`, `removeCartItem`, `updateCartItemQuantity`, `verifyCartTotal`):**

```
com.mobileautomation.framework.exceptions.ElementActionException: Element action 'click' failed on locator: AppiumBy.accessibilityId: Tap to add product to cart
Caused by: org.openqa.selenium.TimeoutException: Expected condition failed: waiting for element to be clickable:
AppiumBy.accessibilityId: Tap to add product to cart (tried for 15 second(s) with 500 milliseconds interval)
    at com.mobileautomation.framework.pages.ProductDetailsPage.addToCart(ProductDetailsPage.java:54)
    at com.mobileautomation.framework.tests.CartTest.addPilotProductToCartAndOpenCart(CartTest.java:879)
```

**`adjustProductQuantity` (TC-011):**

```
java.lang.AssertionError: verifyVisible [Quantity Selector]: expected to be visible
    at com.mobileautomation.framework.assertions.CommonAssertions.evaluate(CommonAssertions.java:83)
```

**`selectProductColor` (TC-010):**

```
java.lang.AssertionError: verifyVisible [Color Selector]: expected to be visible
    at com.mobileautomation.framework.assertions.CommonAssertions.evaluate(CommonAssertions.java:83)
```

---

## Root Cause

The failure screenshot for the CartTest cluster (`reports/screenshots/element_action_click_failure_20260807_113011_0IwlgD.png`, captured for `accessCartScreen` at the moment `addToCart()` times out) shows the app **correctly on the Product Details screen** — "Sauce Labs Backpack (violet)", $29.99, and its star rating are all visible and correctly rendered. Navigation was not the problem (Phase 17.5G's earlier assumption, based on the wrong test's screenshot, is superseded by this finding). **The Add to Cart button, Quantity Selector, and Color Selector are simply not present in that viewport** — the screen's visible content ends at the star rating.

`ProductDetailsPage.scrollToHighlights()` (used successfully by the already-passing `scrollToProductHighlights`, TC-013) proves this screen requires scrolling to reach content below the initially-rendered area — that is the established, working pattern in this exact class. `addToCart()`, `isQuantitySelectorDisplayed()`, and `isColorSelectorDisplayed()` are the only three interaction points on this screen that do **not** apply it, and are exactly the three whose target elements sit below the same fold `scrollToHighlights()` already scrolls past.

**Classification: Synchronization issue.**

- **Not** a locator error — the accessibility ids/resource ids themselves are correct (confirmed by MA-LOC-001 and by the fact `scrollToHighlights()` locates its own target correctly using the identical id-based strategy).
- **Not** a wait-duration problem — 15 seconds already exceeds any plausible render delay; the element is not merely slow to appear, it is off-screen.
- **Not** an emulator limitation requiring documentation-only treatment — the fix (scroll into view before interacting) is a normal, already-proven pattern in this same Page Object, safe on any screen size.
- **Not** a change to verified real-device behavior — `ScrollUtility`'s `scrollToAccessibilityId`/`scrollToResourceId` are built on UiAutomator's own `UiScrollable.scrollIntoView(...)`, which is a no-op when the target is already visible (confirmed by inspecting `ScrollUtility.java` directly) — so on a real device where these controls are already in view, adding this call changes nothing observable.

---

## Evidence

Failure screenshot (`element_action_click_failure_20260807_113011_0IwlgD.png`) confirms correct navigation to Product Details, with the Add to Cart button absent from the visible viewport — quoted from direct visual inspection, not inferred.

`ProductDetailsPage.java` (current state, lines 52–83) confirms `addToCart()`, `isQuantitySelectorDisplayed()`, and `isColorSelectorDisplayed()` contain no scroll call, while `scrollToHighlights()` (lines 127–135, same file) already composes `ScrollUtility.scrollToResourceId(...)` for exactly this reason on the same screen.

`ScrollUtility.java` (lines 57–64) confirms `scrollToResourceId`/`scrollToAccessibilityId` both delegate to UiAutomator's `UiSelector(...).scrollIntoView(...)` mechanism — standard Android tooling behavior, not custom logic, and safe when the target is already on-screen.

Appium session capabilities from the failing run's own log confirm the CI emulator's viewport: `deviceScreenSize: 1080x1920`, `deviceScreenDensity: 420`, `pixelRatio: 2.625`, `viewportRect: {height: 1857, width: 1080}` — a concrete, on-the-record difference in available vertical space from whatever the real-device baseline's screen provides.

---

## Confidence

**High.** The screenshot directly shows the screen state at the moment of failure (visual ground truth, not inference), the missing scroll call is a straightforward code-inspection finding, and the proposed mechanism (`scrollIntoView`) is already proven working elsewhere in this exact class against this exact screen.

---

## Proposed Fix

*(Described only — implemented in the accompanying Phase 17.6B report.)*

Add a `ScrollUtility.scrollToAccessibilityId(...)` call at the start of `addToCart()` and `isColorSelectorDisplayed()`, and before the visibility checks in `isQuantitySelectorDisplayed()`, targeting the same accessibility ids these methods already interact with. Expose the required raw accessibility-id strings in `ProductDetailsLocators` alongside their existing `By` constants, following the exact precedent `PRODUCT_HIGHLIGHTS_RESOURCE_ID`/`PRODUCT_HIGHLIGHTS_LABEL` already establishes in the same file.

---

**End of Document — Phase 17.6A Root Cause Report, v1.0**
