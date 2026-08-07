---
document_id: PHASE-17.6E
title: Product Card Price Synchronization Root Cause Analysis
version: v1.0
status: Final — Root Cause Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6C, PHASE-17.6D]
classification: Internal
---

# Phase 17.6E — Product Card Price Synchronization Root Cause Analysis

| Field | Value |
|---|---|
| Failed Workflow Run | [31200579227](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31200579227) |
| Result of Prior Fix (17.6D) | Confirmed — 19 tests completed, only 3 failed (down from 6). All 4 checkout/payment tests Phase 17.6D targeted now pass. |
| Test Addressed By This Finding | `ProductDetailsTest.addProductToCart` (TC-012) — 1 of the 3 remaining failures |

---

## Executive Summary

Of the 3 failures remaining in run `31200579227`, this report addresses `addProductToCart` (TC-012) — a deterministic, evidence-based Synchronization issue distinct from the other two remaining failures (see Phase 17.6F for disposition of those).

`ProductsPage.getCardPrice(productName)` reads the Pilot Product's price via a parent-axis XPath anchored on its name text, without scrolling immediately before the read. `verifyProductCardExists(productName)` — called earlier in the same test — does scroll (via `scrollToProduct`/`ScrollUtility.scrollToText`), but only far enough to bring the product's *name* into view. A prior run's screenshot (`addProductToCart_failure_20260807_162226_DJJBeY.png`, Phase 17.6 evidence trail) shows the Pilot Product's card at the very bottom edge of the viewport — its image and the start of its name are visible, but the price line is cut off below the visible area.

**Classification: Synchronization issue** — the same defect class as Phase 17.6A/17.6C, in a third location.

---

## Exact Command / Error

```
com.mobileautomation.framework.exceptions.ElementActionException: Element action 'getText' failed on locator:
By.xpath: //*[@text='Sauce Labs Backpack (violet)']/parent::*/*[@content-desc='Product Price']
Caused by: org.openqa.selenium.TimeoutException: Expected condition failed: waiting for visibility of element
located by By.xpath: ...  (tried for 15 second(s) with 500 milliseconds interval)
    at com.mobileautomation.framework.pages.ProductsPage.getCardPrice(ProductsPage.java:139)
    at com.mobileautomation.framework.tests.ProductDetailsTest.addProductToCart(ProductDetailsTest.java:67)
```

---

## Root Cause

`ProductsPage.verifyProductCardExists(productName)` calls `scrollToProduct(productName)` → `ScrollUtility.scrollToText(productName)`, which scrolls only until the product's **name text** satisfies `UiSelector().text(...)` — it does not know or care about the price element positioned below the name within the same card. On a 2-column grid layout, this can leave the name just barely inside the viewport while the price (rendered lower in the same card) remains clipped. `getCardPrice(productName)`, called immediately afterward in the same test, does not re-scroll before resolving its own (different) target element.

This mirrors Phase 17.6A/17.6C exactly: an element resolvable by locator, but not guaranteed visible at the moment it's queried, on a viewport where a real-device baseline apparently never exposed the gap.

---

## Evidence

Screenshot evidence (captured at the moment of this exact failure in an earlier run in this cycle) shows the catalog with the Pilot Product's card ("Sauce Labs Backpack (violet)") at the bottom of the visible grid — its name text partially visible, no price shown for that specific card, while the two fully-visible cards above it (orange, red) both show their price and rating normally.

`ProductsPage.java` (current state) confirms `getCardPrice` (line 138–140) contains no scroll call, unlike `verifyProductCardExists` (line 115–126), which does.

---

## Confidence

**High** — direct visual evidence of the specific element being clipped, and a fix mechanism (`ScrollUtility.scrollToText`) already proven correct by two prior CI-confirmed fixes in this same phase.

---

## Proposed Fix

*(Described only — implemented in the accompanying Phase 17.6F report alongside disposition of the other two remaining failures.)*

Add a `ScrollUtility.scrollToText(productName)` call at the start of `getCardPrice(String productName)`, immediately before it resolves `productPriceForCard(productName)` — guaranteeing the specific card is scrolled into view right before this method's own read, independent of whatever scroll position an earlier call left the screen in.

---

**End of Document — Phase 17.6E Root Cause Report, v1.0**
