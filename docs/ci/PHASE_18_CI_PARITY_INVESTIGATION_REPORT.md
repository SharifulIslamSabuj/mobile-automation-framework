---
document_id: PHASE-18
title: CI Parity Investigation — accessDrawerItems (TC-028)
version: v1.0
status: Final — Investigation and Fix Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-08
last_updated: 2026-08-08
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17_FINAL, MA-LOC-001]
classification: Internal
---

# Phase 18 — CI Parity Investigation: `accessDrawerItems` (TC-028)

| Field | Value |
|---|---|
| Baseline Run | [31211655120](https://github.com/SharifulIslamSabuj/mobile-automation-framework/actions/runs/31211655120) (Phase 17 final — 18/19 passing) |
| Failing Test | `NavigationTest.accessDrawerItems` (TC-028) — 1 of 19 |
| Artifacts Reviewed | ExtentReports HTML, Gradle test report/XML, `logs/automation.log`, all `tc028_*` and `accessDrawerItems_failure_*` screenshots, `NavigationTest.java`, `NavigationDrawerPage.java`, `DrawerDestinationLocators.java`, `MA-LOC-001` §15.7/§17 |

---

## 1. Executive Summary

Full artifact review confirms the FingerPrint destination screen **loads correctly** — its title, toggle, and description text are all present and rendered — but a native Android `AlertDialog` titled "Biometrics" appears on top of it, blocking the test's visibility check for the screen title. This dialog is the AUT's own (correct) reaction to the CI emulator having no biometric hardware configured; it is not present in this project's real-device evidence.

Unlike Phase 17's disposition of this same finding, this investigation found that the dialog is **structurally identical** to the Reset App State dialog this framework already handles successfully — both are standard Android platform `AlertDialog`s, and Phase 15.1's live-device evidence already verified the platform button id (`android:id/button1`) this exact AUT uses for that dialog class. Reusing that already-verified id, rather than inventing a new one, makes a real fix possible without guessing.

**Classification: C (Synchronization issue) at the test-design level, rooted in F/H (Emulator limitation / Environment difference) at the platform level.** The environment difference itself is not fixable and is documented as such; the test's lack of tolerance for a system dialog it should defensively handle — exactly as it already does for Reset App State — is fixable, and is fixed in this report.

---

## 2. Cluster Analysis

Only one cluster — one failing test, one root cause.

### Cluster A
**1 failure — `NavigationTest.accessDrawerItems` (TC-028)**
**Root cause:** A native Android "Biometrics not supported" `AlertDialog` appears when the FingerPrint drawer destination loads on a device/emulator without biometric hardware, blocking the test's visibility check for the underlying (correctly-rendered) screen title.

---

## 3. Evidence

### 3.1 Exact Failure Point

```
java.lang.AssertionError: verifyVisible [FingerPrint screen — title]: expected to be visible
    at com.mobileautomation.framework.assertions.CommonAssertions.evaluate(CommonAssertions.java:83)
    at com.mobileautomation.framework.tests.NavigationTest.verifyDrawerDestination(NavigationTest.java:105)
    at com.mobileautomation.framework.tests.NavigationTest.accessDrawerItems(NavigationTest.java:66)
```

(Confirmed identically across every run in Phase 17 in which this test executed — runs 5 through 10.)

### 3.2 Screenshot Evidence

`tc028_06_fingerprint_destination_*.png` — captured immediately after tapping the drawer's "FingerPrint" item, before the visibility assertion runs. Shows the navigation drawer still open/mid-transition.

`accessDrawerItems_failure_*.png` — captured at the moment of assertion failure. Shows:
- The FingerPrint screen fully and correctly rendered underneath: title "FingerPrint", "Allow login with FingerPrint" toggle (off), descriptive body text, and the demo disclaimer text.
- A modal `AlertDialog` titled **"Biometrics"** on top of it, reading: *"Biometric is or not supported or not enable on your device. Please check your device or your settings."*, with a single **"OK"** button.

This is direct, conclusive visual evidence that the destination screen itself loaded correctly — the failure is not a navigation, timing, or locator defect on the FingerPrint screen itself.

### 3.3 Source Code Evidence

`DrawerDestinationLocators.java` documents `FINGERPRINT_TITLE = By.id(PACKAGE + "bioMetricTV")`, confirmed correct and unchanged (MA-LOC-001 §15.7 — this exact id, matched against `fragment_biometric.xml` line 11, "Unique"). The locator itself was never in question.

The same file documents the **Reset App State dialog** as "a standard Android platform `AlertDialog`, not app-owned XML" (MA-LOC-001 §17), with its title, message, and buttons resolved via a mix of app- and platform-namespaced ids — critically, `RESET_DIALOG_MESSAGE = By.id("android:id/message")` and `RESET_DIALOG_CONFIRM_BUTTON = By.id("android:id/button1")`, both **platform-level ids, independently verified via a live `uiautomator dump` on this project's own real device (Phase 15.1)**, not invented or assumed.

Android's standard `AlertDialog` (built via `AlertDialog.Builder`) uses these same platform-level view ids for *any* instance of that dialog class, regardless of which app feature constructs it — this is OS/SDK-level dialog chrome, not custom per-screen styling. The "Biometrics" dialog observed in the screenshot has the identical visual structure (bold title, message body, single bottom-right button) as the already-verified Reset dialog. This gives strong, non-speculative grounds — an already-proven id in this exact AUT, not a guess — for the button locator used in the fix below.

### 3.4 Automation Log Evidence

```
Assertion failed: verifyVisible [FingerPrint screen — title]: expected to be visible
```

No exception, no timeout stack trace beyond the standard `WaitUtility` visibility-wait exhaustion — confirming this is a clean "element exists but is obscured/not interactable" outcome, not an element-not-found error, consistent with the screenshot showing the title element present underneath the dialog.

---

## 4. Classification

| Category | Applies? | Reasoning |
|---|---|---|
| A. Framework defect | No | `ElementActions`, `WaitUtility`, and `CommonAssertions` all behaved correctly — they accurately reported the title as not visible, because it genuinely was not (obscured). |
| B. Test design issue | **Partially** | `verifyDrawerDestination`'s generic flow assumes no interstitial dialog can appear between tapping a drawer item and the destination being checkable — true for six of seven destinations, false for FingerPrint on this environment. |
| C. Synchronization issue | **Yes — primary, fixable cause** | The test does not wait for/handle a transient system dialog before checking the underlying screen. |
| D. Locator issue | No | `FINGERPRINT_TITLE` is correct and unchanged; the fix reuses an already-verified platform id, not a new one. |
| E. Viewport / scrolling | No | The dialog is centered in the viewport, not off-screen — this is not a below-the-fold defect like Phase 17's other findings. |
| F. Emulator limitation | **Yes — root cause of the dialog's existence** | The CI emulator has no configured biometric hardware; the dialog is the AUT's genuine, correct reaction to that fact. |
| G. AUT defect | No | The AUT is behaving correctly — informing the user biometrics are unavailable is expected, desired behavior on hardware without biometric support. |
| H. Environment difference | **Yes — same evidence as F** | This project's real-device baseline (a physical device) does not exhibit this dialog in its own evidence trail. |
| I. Unknown | No | Fully explained by A–H above; no unexplained residue. |

**Net classification: F/H at the platform level (not fixable, documented), C/B at the test level (fixable without touching the platform-level cause).**

---

## 5. Why This Fix Is Correct, Safe, and Preferable to Alternatives

**Why this is the correct fix:** the test should tolerate a system-level interstitial the same way it already tolerates the Reset App State dialog and OS permission dialogs (`PermissionDialogComponent`, `execution.autoGrantPermissions=true`) — this is consistent, established framework precedent, not a new pattern invented for this one case.

**Why it will not break the verified real-device baseline:** the fix is conditional — `isBiometricsDialogDisplayed()` is checked first, and the dismiss step only executes if the dialog is actually present. On the real-device baseline, where biometric hardware exists and this dialog has never appeared in this project's evidence, the check returns false and the dismiss step never runs — the test's behavior on real hardware is unchanged.

**Why this is preferable to alternatives:**
- *Removing/skipping the FingerPrint destination from TC-028* — prohibited outright ("never skip tests," "never mark tests ignored"), and would reduce real coverage that already exists and passes on real hardware.
- *Weakening the assertion (e.g., checking only that some element is displayed)* — prohibited ("never weaken assertions"), and would stop verifying the actual acceptance criterion (the FingerPrint screen is reachable).
- *Increasing the wait timeout* — would not help; the dialog is not slow to disappear, it is modal and requires explicit dismissal. This would be a "speculative wait," explicitly prohibited.
- *Documenting without fixing* — was Phase 17's disposition, made without the deeper investigation into the Reset dialog's already-verified platform ids this report performed. That investigation makes a real fix newly possible; leaving it undone once a legitimate, evidence-backed fix is available under-serves this phase's own preference for Option A over Option B.

---

## 6. Fix

### Files Modified

| File | Change |
|---|---|
| `src/main/java/com/mobileautomation/framework/locators/DrawerDestinationLocators.java` | Added `BIOMETRICS_DIALOG_OK_BUTTON`, reusing the already-verified `android:id/button1` platform id |
| `src/test/java/com/mobileautomation/framework/pages/NavigationDrawerPage.java` | Added `isBiometricsDialogDisplayed()` and `dismissBiometricsDialog()`, mirroring the existing Reset-dialog method pair exactly |
| `src/test/java/com/mobileautomation/framework/tests/NavigationTest.java` | FingerPrint destination handling moved out of the generic `verifyDrawerDestination` loop and inlined (mirroring how Reset App State is already handled inline), with one added conditional dismiss step |

### Exact Changes

```diff
+    /**
+     * Biometrics "not supported" dialog — a standard Android platform
+     * {@code AlertDialog}, structurally identical to the Reset App State
+     * dialog above (title + message + single button). Appears when the
+     * FingerPrint screen loads on a device/emulator without biometric
+     * hardware configured. Reuses {@code android:id/button1} — the exact
+     * same platform id already verified correct for this AUT's Reset
+     * dialog via live-device evidence (Phase 15.1) — not a new or invented
+     * locator, the same OS-level id applied to a second occurrence of the
+     * same OS dialog widget.
+     */
+    public static final By BIOMETRICS_DIALOG_OK_BUTTON = By.id("android:id/button1");
```

```diff
+    /** @return whether the Biometrics "not supported" dialog (a standard Android platform {@code AlertDialog}) is currently displayed — appears when the FingerPrint screen loads on a device/emulator without biometric hardware. */
+    public boolean isBiometricsDialogDisplayed() {
+        return elementActions.isDisplayed(DrawerDestinationLocators.BIOMETRICS_DIALOG_OK_BUTTON);
+    }
+
+    /** Dismisses the Biometrics "not supported" dialog via its OK button. */
+    public void dismissBiometricsDialog() {
+        elementActions.click(DrawerDestinationLocators.BIOMETRICS_DIALOG_OK_BUTTON);
+    }
```

```diff
-        verifyDrawerDestination(productsPage, drawerPage, "FingerPrint",
-                drawerPage::isFingerprintTitleDisplayed, "FingerPrint screen — title", "tc028_06_fingerprint");
+        // FingerPrint — handled inline, not via verifyDrawerDestination, because this AUT shows a
+        // native "Biometrics not supported" AlertDialog on load when the device/emulator has no
+        // biometric hardware (confirmed absent on this CI emulator; not seen in this project's
+        // real-device evidence). Dismissed only if present -- a no-op on hardware where it never
+        // appears (Phase 18 investigation).
+        logger.info("Selecting drawer item: FingerPrint.");
+        productsPage.tapMenu();
+        CommonAssertions.verifyVisible(productsPage.isDrawerItemDisplayed("FingerPrint"), "Navigation Drawer — FingerPrint item");
+        productsPage.tapDrawerItem("FingerPrint");
+        ScreenshotManager.captureScreenshot("tc028_06_fingerprint_destination");
+        if (drawerPage.isBiometricsDialogDisplayed()) {
+            logger.info("Biometrics dialog present (no biometric hardware on this device/emulator) — dismissing.");
+            drawerPage.dismissBiometricsDialog();
+        }
+        CommonAssertions.verifyVisible(drawerPage.isFingerprintTitleDisplayed(), "FingerPrint screen — title");
+        drawerPage.navigateBack();
+        ScreenshotManager.captureScreenshot("tc028_06_fingerprint_returned_to_catalog");
+        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after returning from FingerPrint)");
```

No other line in any of the three files was changed. No other test, Page Object, or locator was touched.

---

## 7. Validation

- `./gradlew compileJava compileTestJava --no-daemon` — `BUILD SUCCESSFUL` (see commit for confirmation).
- No assertion weakened — `verifyVisible [FingerPrint screen — title]` is still checked exactly as before; only a conditional dismissal was inserted ahead of it.
- No new wait mechanism — `isBiometricsDialogDisplayed()` reuses the same `elementActions.isDisplayed()` pattern (explicit-wait-based) already used by every other conditional check in this framework.
- No locator invented — `android:id/button1` is the exact id already verified correct for this AUT's Reset dialog (MA-LOC-001 §17, Phase 15.1 live-device evidence).
- CI verification: pending the next run (this fix has not yet been observed passing in CI at the time this report section was written — see Section 9).

### Before vs. After (pending CI confirmation)

```
Run 10 (baseline)
-----------------
19 tests
1 failed (accessDrawerItems)

↓ (expected, pending verification)

Run 11
------
19 tests
0 failed
```

---

## 8. Remaining Risks

- **Unverified assumption:** `android:id/button1` is assumed, on strong structural precedent, to be this specific Biometrics dialog's OK button id. It has not been independently confirmed via a fresh `uiautomator dump` of this exact dialog (unlike the original Reset dialog investigation, which did do this). If the assumption is wrong, the dismiss click will fail with a clear, diagnosable `ElementActionException` on the next run — not a silent failure — and this report's Section 9 will disclose that outcome honestly rather than claim success prematurely.
- **Timing:** `isBiometricsDialogDisplayed()` adds up to the full explicit-wait timeout (15s) on any run/device where the dialog does not appear (including the real-device baseline), since a "not present" result must exhaust the wait. This is a minor, bounded latency cost, not a correctness risk, and matches the cost every other defensive `isDisplayed()` check in this framework already incurs.
- **Residual scope:** this fix addresses only the FingerPrint destination. If any other of the six other drawer destinations were ever found to trigger an analogous dialog on some future environment, it would need its own evidence-based investigation — this report does not claim broader coverage than what was actually observed.

---

## 9. CI Verification Result

*(To be completed after the next real run.)*

---

## 10. Recommendation

Pending Section 9's CI confirmation: if the fix resolves the failure, **Option A (19/19)** is achieved and the framework's CI baseline requires no further disclosed limitations. If the assumption in Section 8 proves wrong, the specific, diagnosable failure evidence from that run will be used for one further, narrowly-scoped iteration — not a reversion to undocumented guessing.

---

## Approval

| Role | Name | Status | Date |
|---|---|---|---|
| Prepared By | Project Owner / Repository Maintainer | Submitted | 2026-08-08 |
| Document Status | Final — Investigation and Fix Report (CI verification pending) | — | — |

---

**End of Document — Phase 18 CI Parity Investigation Report, v1.0**
