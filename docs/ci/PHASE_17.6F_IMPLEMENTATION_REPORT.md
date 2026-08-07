---
document_id: PHASE-17.6F
title: Product Card Price Fix and Disposition of Remaining Findings
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6E]
classification: Internal
---

# Phase 17.6F — Product Card Price Fix and Disposition of Remaining Findings

| Field | Value |
|---|---|
| Governing Document | [PHASE_17.6E_ROOT_CAUSE_REPORT.md](PHASE_17.6E_ROOT_CAUSE_REPORT.md) |
| Status | Final — Implementation Report |

## Files Modified

| File | Change |
|---|---|
| `src/test/java/com/mobileautomation/framework/pages/ProductsPage.java` | Added one `scrollToProduct(productName)` call to `getCardPrice(String)`, before its existing price read — no other line changed |

No other file touched.

## Exact Change

```diff
     public String getCardPrice(String productName) {
+        scrollToProduct(productName);
         return elementActions.getText(ProductsLocators.productPriceForCard(productName));
     }
```

## Validation

- `./gradlew compileJava compileTestJava --no-daemon` — `BUILD SUCCESSFUL`.
- Reuses `scrollToProduct(String)`, an existing method already called elsewhere in this same class (`verifyProductCardExists`) — no new scrolling logic introduced, no locator value changed, no assertion weakened, no `Thread.sleep()`.

## Risk

**Low** — same `ScrollUtility` no-op-when-visible guarantee as Phases 17.6B/17.6D, now applied a third time with the same evidentiary basis.

---

## Disposition of the Other Two Remaining Failures

Per Step 5, these are classified and dispositioned here rather than modified in code.

### `NavigationTest.accessDrawerItems` (TC-028) — Known emulator limitation, document only

**Classification: Emulator limitation.** The failure screenshot shows the AUT's own native "Biometrics" dialog ("Biometric is or not supported or not enable on your device") covering the expected FingerPrint screen title. This is the AUT reacting correctly to the CI emulator's genuine absence of biometric hardware — not a defect in the test, the locator, or the framework. Modifying `NavigationTest` to dismiss or expect this dialog would encode emulator-specific behavior into a test whose real-device baseline never needed it, which is exactly what this phase's rules prohibit ("do not change verified real-device behaviour unless there is objective evidence the framework itself is incorrect" — there is no such evidence here; the framework and test are both correct, the AUT's own behavior differs by environment). **Disposition: document as a known CI/emulator limitation in the final baseline qualification report; no code change.**

### `CartTest.accessCartScreen` (run `31200579227` only) — Suspected transient infrastructure issue, not yet a confirmed pattern

**Classification: Infrastructure bug (suspected, unconfirmed).** This exact test passed in the immediately preceding run (`31196174453`) using identical code. In this run, its failure screenshot shows the Android **home screen/launcher** — not the AUT at all — and the log shows an emulator-level error immediately adjacent (`ERROR | Failed to find ColorBuffer: 778`), consistent with a rendering-pipeline glitch that returned the foreground to the launcher mid-test. This does not match the profile of a code defect (no locator, timing, or assertion pattern recurs from a prior finding) and does not match Phase 17.6A/C/E's evidence shape (a specific element clipped by scroll position) — the entire app was gone, not one element. **Disposition: do not modify code on a single unreproduced occurrence. Re-observe on the next run; if it does not recur, document as an isolated, non-blocking CI infrastructure event; if it recurs, open a new root-cause cycle with its own evidence.**

## Remaining Issues Going Into the Next Run

- `getCardPrice` fix (this report) — unverified until the next real run.
- `accessDrawerItems` — expected to still fail (undocumented-until-final-report, not fixed by design).
- `accessCartScreen` — expected to pass (its prior failure is not attributed to any code state this fix changes); its outcome on the next run is itself the evidence needed to confirm or refute transience.

---

**End of Document — Phase 17.6F Implementation Report, v1.0**
