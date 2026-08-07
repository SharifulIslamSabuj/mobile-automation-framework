---
document_id: PHASE-17.6J
title: Add To Cart Button Visibility Check — Implementation Report
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6I]
classification: Internal
---

# Phase 17.6J — Add To Cart Button Visibility Check — Implementation Report

| Field | Value |
|---|---|
| Governing Document | [PHASE_17.6I_ROOT_CAUSE_REPORT.md](PHASE_17.6I_ROOT_CAUSE_REPORT.md) |
| Status | Final — Implementation Report |

## File Modified

`src/test/java/com/mobileautomation/framework/pages/ProductDetailsPage.java` — `isAddToCartButtonDisplayed()` now calls `ScrollUtility.scrollToAccessibilityId(...)` before its existing visibility check, mirroring `addToCart()` exactly. No other file touched.

## Exact Change

```diff
     public boolean isAddToCartButtonDisplayed() {
+        ScrollUtility.scrollToAccessibilityId(ProductDetailsLocators.ADD_TO_CART_BUTTON_ACCESSIBILITY_ID);
         return elementActions.isDisplayed(ProductDetailsLocators.ADD_TO_CART_BUTTON);
     }
```

## Validation

- `./gradlew compileJava compileTestJava --no-daemon` — `BUILD SUCCESSFUL`.
- Reuses the exact constant and method already used by `addToCart()` (Phase 17.6A) — no new locator, no new scroll mechanism.
- No assertion weakened, no test disabled, no `Thread.sleep()`.

## Risk

**Low** — same `scrollToAccessibilityId` no-op-when-visible guarantee already confirmed working across four prior fixes in this phase (17.6A/B, 17.6C/D).

## Status of All Findings Going Into the Next Run

| Finding | Status |
|---|---|
| Cart shared-helper Add to Cart (17.6A/B) | Confirmed fixed (run 6) |
| Checkout To Payment button (17.6C/D) | Confirmed fixed (run 7) |
| `accessCartScreen` single-run failure | Confirmed transient, not a defect (run 8 passed unmodified) |
| `getCardPrice` (17.6E–H) | Confirmed fixed (run 9 — reached and passed all downstream Product Details identity checks) |
| `isAddToCartButtonDisplayed` (this report) | Unverified until next run |
| `accessDrawerItems` (biometric dialog) | Unchanged — documented limitation, no code fix planned |

If this fix is confirmed, `accessDrawerItems` (the emulator biometric-hardware limitation) is expected to be the **only** remaining failure, at which point the loop's Option B stop condition (Phase 17 mandate) is met.

---

**End of Document — Phase 17.6J Implementation Report, v1.0**
