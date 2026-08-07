---
document_id: PHASE-17.5G
title: CI Pipeline Stabilized — Emulator/Real-Device Parity Findings
version: v1.0
status: Final — Interim Findings Report (Autonomous Loop Paused By Design)
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.4A, PHASE-17.4B, PHASE-17.5A, PHASE-17.5B, PHASE-17.5C, PHASE-17.5D, PHASE-17.5E, PHASE-17.5F, MA-CICD-002]
classification: Internal
---

# Phase 17.5G — CI Pipeline Stabilized; Emulator/Real-Device Parity Findings

| Field | Value |
|---|---|
| GitHub Actions Run | [31174085507](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31174085507) |
| Triggering Commit | `6d11d7c` — the Phase 17.5F fix (gradlew executable bit) |
| Job Duration | 13m3s (by far the longest run to date — the first to actually execute the suite) |
| Result | `failure` (Gradle `test` task failed: 19 tests completed, 16 failed, 3 passed) |
| Artifacts | `mobile-automation-run-5` — uploaded successfully for the first time (ExtentReports HTML, logs, screenshots, Gradle test results) |

---

## Why This Report Breaks the Established A/B Cycle Pattern

Phases 17.5A–17.5F each found and fixed exactly one shell-syntax or repository-configuration defect, with a single, unambiguous, mechanically-verifiable root cause. This run is different in kind: for the first time, the **pipeline itself worked completely** — checkout, JDK/Gradle setup, KVM, Appium install, APK download, emulator boot, Appium startup, Appium reachability, Gradle build validation, and full TestNG execution across all 19 test cases, followed by successful report generation and artifact upload. Every stage MA-CICD-002 §7 specified now executes.

What failed is the **test suite's outcome on this new environment**, not the pipeline's mechanics. Per this phase's own instructions ("never guess," "never modify working components," and MA-CICD-002 §14's explicit requirement to treat CI results as a parity-verification exercise rather than an assumption of real-device equivalence), diagnosing *why* 16 tests fail requires application-behavior investigation, not a workflow-syntax fix — and any code change to the framework's Page Objects, waits, or assertions is a fundamentally different, higher-risk category of action than the four fixes already made, because it risks altering behavior that is currently correct and verified against this project's real-device baseline (MA-TC-001). That is a decision this report surfaces with full evidence rather than one this phase makes unilaterally.

**The autonomous run→observe→fix→push loop is paused here by design**, not because it hit an unresolvable blocker.

---

## Result Summary

| | |
|---|---|
| Total automated test cases | 19 |
| Passed | 3 — `LoginTest.verifyLoginOutcome` (TC-004), `CartTest.sortProductCatalog` (TC-006), `ProductDetailsTest.scrollToProductHighlights` (TC-013) |
| Failed | 16 |

The 16 failures resolve to **three distinct, independently-evidenced root causes** — not 16 independent problems:

### Cluster 1 — `addToCart()` timeout (12 of 16 failures)

All 11 `CartTest` methods that depend on a shared "add a product to cart" setup helper (`CartTest.java:879`, calling `ProductDetailsPage.addToCart()`), plus `ProductDetailsTest.addProductToCart` (TC-012) directly, fail identically:

```
org.openqa.selenium.TimeoutException: Expected condition failed: waiting for element to be clickable:
AppiumBy.accessibilityId: Tap to add product to cart (tried for 15 second(s) with 500 milliseconds interval)
    at com.mobileautomation.framework.pages.ProductDetailsPage.addToCart(ProductDetailsPage.java:54)
```

**This is a single systematic condition, not 12 flaky failures** — it reproduced 12/12 times. The captured failure screenshot (`addProductToCart_failure_*.png`) shows the app still on the **Products Catalog** screen at the moment of failure, not the Product Details screen `addToCart()` expects — meaning the preceding `productDetailsPage.isDisplayed()` check passed even though the app had not actually navigated to Product Details. This points at either a navigation-timing gap or an over-permissive `isDisplayed()` check on this environment, not a missing/renamed locator.

### Cluster 2 — Emulator has no biometric hardware (1 of 16 failures)

`NavigationTest.accessDrawerItems` (TC-028) fails at the "FingerPrint" drawer destination:

```
java.lang.AssertionError: verifyVisible [FingerPrint screen — title]: expected to be visible
    at com.mobileautomation.framework.tests.NavigationTest.verifyDrawerDestination(NavigationTest.java:105)
```

The failure screenshot shows the AUT's own native **"Biometrics" dialog** — *"Biometric is or not supported or not enable on your device. Please check your device or your settings."* — covering the expected screen title. This is a well-understood, well-documented category of Android emulator limitation: the AVD provisioned for this pipeline (Phase 17.2B Decision 3) has no configured biometric/fingerprint hardware simulation, so the AUT's own biometric-capability check surfaces this dialog, which the test does not currently account for. Every other drawer destination visited before FingerPrint (WebView, QR Code Scanner, Geo Location, Drawing, About) is confirmed successful via their own screenshots in this same run.

### Cluster 3 — Element visibility mismatches on Product Details (2 of 16 failures)

`ProductDetailsTest.selectProductColor` (TC-010) and `ProductDetailsTest.adjustProductQuantity` (TC-011) each fail with a simple, direct assertion, on a screen confirmed (by screenshot) to have loaded correctly:

```
java.lang.AssertionError: verifyVisible [Color Selector]: expected to be visible      (ProductDetailsPage.java:127)
java.lang.AssertionError: verifyVisible [Quantity Selector]: expected to be visible   (ProductDetailsPage.java:163)
```

Unlike Cluster 1, the Product Details screen itself is confirmed visually correct at failure time (product name, price, and rating all render as expected) — the specific Color/Quantity Selector elements are simply not found where expected. This could be a scroll-position, screen-dimension/DPI, or rendering-order difference between the `pixel` AVD profile and the real vivo I2301 device this framework's locators were originally verified against (MA-LOC-001).

---

## What This Report Is Not Doing

Per this phase's explicit rules, no Java source, Page Object, locator, wait strategy, or assertion was modified while producing this report. No fix is proposed for implementation here. The three clusters above are evidence-based classifications, not confirmed single-line root causes at the level of rigor Phases 17.5A–17.5F achieved for the shell/permission defects — each would need further investigation (at minimum: reviewing `ProductDetailsPage.isDisplayed()`'s exact wait condition for Cluster 1, and comparing the AVD's actual rendered viewport against MA-LOC-001's real-device-verified layout assumptions for Cluster 3) before any change could be evidence-justified rather than guessed.

---

## Recommendation

This is the natural point to report back rather than continue autonomously, because the next step is a genuine decision, not another mechanical fix:

1. **Treat this as the expected first-run parity-verification outcome** MA-CICD-002 §14 anticipated, document it as a known CI/real-device gap, and decide separately (in a dedicated phase) whether/how to address each cluster — this keeps today's real-device baseline (MA-TC-001) untouched while the emulator-specific gaps are investigated on their own timeline.
2. **Or** authorize continuing the autonomous loop into framework-level investigation now, with the understanding that any fix here (e.g., strengthening `isDisplayed()`, adding a biometric-dialog dismissal step, or adjusting a wait/scroll for Color/Quantity Selectors) touches working, real-device-verified code and should be evidence-driven per cluster, likely requiring several more real CI runs to verify each independently.

Either way, the CI/CD pipeline itself — the actual subject of Phases 17.0 through 17.5F — is now confirmed mechanically complete and functional end-to-end for the first time.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-07 |
| Document Status | Final — Interim Findings Report | — | — |

---

**End of Document — Phase 17.5G Interim Findings Report, v1.0**
