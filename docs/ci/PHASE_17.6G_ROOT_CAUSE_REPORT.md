---
document_id: PHASE-17.6G
title: Product Card Price — Fix Verification Failure and Deeper Root Cause
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6E, PHASE-17.6F]
classification: Internal
---

# Phase 17.6G — Product Card Price: Fix Verification Failure and Deeper Root Cause

| Field | Value |
|---|---|
| Failed Workflow Run | [31207000563](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31207000563) |
| Result | 19 tests completed, 2 failed — `accessDrawerItems` (known, undisputed) and `addProductToCart` (TC-012) — **Phase 17.6F's fix did not resolve this failure.** |

---

## Executive Summary

`CartTest.accessCartScreen`, dispositioned in Phase 17.6F as a suspected one-off transient emulator glitch, **passed** in this run — confirming that disposition was correct and required no code change.

`ProductDetailsTest.addProductToCart` (TC-012) **still failed**, at the identical location, with the identical exception, despite Phase 17.6F's fix (`getCardPrice()` now calls `scrollToProduct(productName)` before reading the price). The failure screenshot for this run is **visually identical** to the one that produced the original Phase 17.6E finding — the viewport did not move at all as a result of the added scroll call.

This report documents that the Phase 17.6F fix was ineffective, establishes why, and proposes a different, more targeted correction.

---

## Why the Prior Fix Did Not Work

`ScrollUtility.scrollToText(productName)` (invoked by `scrollToProduct`) delegates to Android's `UiScrollable.scrollIntoView(new UiSelector().text(productName))`. This API scrolls only until an element matching the selector is present and minimally visible in the accessibility hierarchy — it does not scroll further once that minimal criterion is met, even if only a sliver of the element (or, as here, only the element the selector targets, not sibling content below it) is actually on screen.

`verifyProductCardExists(productName)` already calls this exact scroll once, and by the time `getCardPrice(productName)` calls it a second time (Phase 17.6F's fix), the name text already satisfies the selector's criteria at its current (barely-visible) scroll position — so the second call is a genuine no-op, not a partial improvement. This is confirmed empirically: the failure screenshot from this run and the screenshot from the run that produced the original Phase 17.6E finding are pixel-identical in layout.

Scrolling further specifically to the price element is not straightforward: `productPriceForCard`'s content-desc (`"Product Price"`) is not unique — every product card's price shares it — so `scrollToAccessibilityId("Product Price")` would resolve to whichever price element is first found (not necessarily the Pilot Product's), which could be already-visible and equally unhelpful.

---

## Classification

**Synchronization issue** (unchanged classification from Phase 17.6E) — but the specific mechanism is different from Phase 17.6A/17.6C/17.6E's simple "scroll to the target's own accessibility id/resource id": those targets could each be scrolled to directly and unambiguously. This target (a specific product card's price) has no unique locator to scroll to directly, requiring a different scrolling strategy — an explicit incremental scroll rather than a criteria-based `scrollIntoView`.

---

## Confidence

**High** on the diagnosis (identical before/after screenshots are direct, conclusive evidence the added scroll call did not move the viewport). **Medium** on the proposed fix's completeness — an incremental scroll is a reasonable next step per the evidence available, but has not itself been verified by a run yet.

---

## Proposed Fix

*(Described only — implemented in the accompanying Phase 17.6H report.)*

Replace the redundant `scrollToProduct` re-call in `getCardPrice()` with one additional `ScrollUtility.scrollDown()` call after it — a generic, unconditional forward scroll (distinct from the criteria-based `scrollIntoView`) to move the viewport further and expose the price line below the already-visible name. This is the smallest available primitive that can move the viewport past what `scrollIntoView` considers "already satisfied."

---

**End of Document — Phase 17.6G Root Cause Report, v1.0**
