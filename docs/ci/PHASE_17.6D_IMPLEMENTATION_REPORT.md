---
document_id: PHASE-17.6D
title: To Payment Button Synchronization Fix — Implementation Report
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6C]
classification: Internal
---

# Phase 17.6D — To Payment Button Synchronization Fix — Implementation Report

| Field | Value |
|---|---|
| Governing Document | [PHASE_17.6C_ROOT_CAUSE_REPORT.md](PHASE_17.6C_ROOT_CAUSE_REPORT.md) |
| Status | Final — Implementation Report |

## Files Modified

| File | Change |
|---|---|
| `src/main/java/com/mobileautomation/framework/locators/CheckoutLocators.java` | Added `TO_PAYMENT_BUTTON_ACCESSIBILITY_ID` raw string constant, exposed alongside the existing `TO_PAYMENT_BUTTON` `By` constant |
| `src/test/java/com/mobileautomation/framework/pages/CheckoutPage.java` | Added `ScrollUtility` import; added one `ScrollUtility.scrollToAccessibilityId(...)` call to `isToPaymentButtonDisplayed()`, before its existing visibility check |

No other file touched.

## Exact Changes

```diff
+    public static final String TO_PAYMENT_BUTTON_ACCESSIBILITY_ID = "Saves user info for checkout";
-    public static final By TO_PAYMENT_BUTTON = AppiumBy.accessibilityId("Saves user info for checkout");
+    public static final By TO_PAYMENT_BUTTON = AppiumBy.accessibilityId(TO_PAYMENT_BUTTON_ACCESSIBILITY_ID);
```

```diff
+import com.mobileautomation.framework.utils.ScrollUtility;
```

```diff
     public boolean isToPaymentButtonDisplayed() {
+        ScrollUtility.scrollToAccessibilityId(CheckoutLocators.TO_PAYMENT_BUTTON_ACCESSIBILITY_ID);
         return elementActions.isDisplayed(CheckoutLocators.TO_PAYMENT_BUTTON);
     }
```

## Validation

- `./gradlew compileJava compileTestJava --no-daemon` — `BUILD SUCCESSFUL`.
- Identical pattern to the already-CI-confirmed Phase 17.6B fix — same `ScrollUtility` method, same no-op-if-visible guarantee, no locator value changed, no assertion weakened, no `Thread.sleep()`, no test disabled.

## Risk

**Low** — same justification as Phase 17.6B, now with additional confirmation: Phase 17.6B's identical fix pattern was verified by a real CI run (31196174453) to resolve its 13 targeted failures with no observed regression elsewhere.

## Remaining Issues

Two failures remain, still unaddressed:

- `NavigationTest.accessDrawerItems` (TC-028) — emulator biometric-hardware dialog.
- `ProductDetailsTest.addProductToCart` (TC-012) — `ProductsPage.getCardPrice()` parent-axis XPath timeout, under active investigation (preliminary hypothesis: product card only partially scrolled into view before price lookup — not yet confirmed with fresh evidence).

---

**End of Document — Phase 17.6D Implementation Report, v1.0**
