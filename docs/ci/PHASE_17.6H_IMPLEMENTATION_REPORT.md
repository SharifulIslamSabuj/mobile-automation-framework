---
document_id: PHASE-17.6H
title: Product Card Price — Incremental Scroll Fix Implementation
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6G]
classification: Internal
---

# Phase 17.6H — Product Card Price: Incremental Scroll Fix Implementation

| Field | Value |
|---|---|
| Governing Document | [PHASE_17.6G_ROOT_CAUSE_REPORT.md](PHASE_17.6G_ROOT_CAUSE_REPORT.md) |
| Status | Final — Implementation Report |

## File Modified

`src/test/java/com/mobileautomation/framework/pages/ProductsPage.java` — `getCardPrice(String)` now calls `ScrollUtility.scrollDown()` after the existing `scrollToProduct(productName)` call, before reading the price. No other file touched.

## Exact Change

```diff
     public String getCardPrice(String productName) {
         scrollToProduct(productName);
+        ScrollUtility.scrollDown();
         return elementActions.getText(ProductsLocators.productPriceForCard(productName));
     }
```

## Why This Attempt Differs From the Prior (Failed) One

Phase 17.6F's fix re-called `scrollToProduct` (a criteria-based `scrollIntoView`), which Phase 17.6G's evidence showed was a no-op once the name text already minimally satisfied its own visibility criterion. `scrollDown()` is a different primitive — an unconditional one-page forward scroll (`UiScrollable.scrollForward()`) — capable of moving the viewport regardless of what `scrollIntoView` already considers satisfied.

## Validation

- `./gradlew compileJava compileTestJava --no-daemon` — `BUILD SUCCESSFUL`.
- No locator changed, no assertion weakened, no `Thread.sleep()`, no test disabled.
- `ScrollUtility` was already imported in this file (used by `scrollToProduct`) — no new import needed.

## Risk

**Medium** (explicitly higher than Phases 17.6B/17.6D/17.6F's low-risk, no-op-safe fixes) — an unconditional forward scroll, unlike a criteria-based one, is not inherently safe from overshoot. If the Pilot Product's row is not fully revealed by one page-forward scroll from its current (partially-visible) position, or if the scroll moves far enough to push the target row's price back off the *top* of the viewport, this fix could fail differently rather than succeed. This risk is disclosed rather than hidden; the next CI run is the verification step, consistent with this phase's evidence-first discipline.

## Remaining Issues

- `NavigationTest.accessDrawerItems` — unchanged disposition from Phase 17.6F: document as a known emulator biometric-hardware limitation, no code change.
- This fix itself — unverified until the next run.

---

**End of Document — Phase 17.6H Implementation Report, v1.0**
