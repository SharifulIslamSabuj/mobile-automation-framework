---
document_id: PHASE-17.6B
title: Cart/Quantity/Color Selector Synchronization Fix — Implementation Report
version: v1.0
status: Final — Implementation Report
author: Project Owner / Repository Maintainer
created_date: 2026-08-07
last_updated: 2026-08-07
reviewed_by: Pending
approved_by: Pending
project: Mobile Automation Framework
project_code: MA
related_documents: [PHASE-17.6A]
classification: Internal
---

# Phase 17.6B — Synchronization Fix — Implementation Report

| Field | Value |
|---|---|
| Governing Document | [PHASE_17.6A_ROOT_CAUSE_REPORT.md](PHASE_17.6A_ROOT_CAUSE_REPORT.md) |
| Status | Final — Implementation Report |

## Files Modified

| File | Change |
|---|---|
| `src/main/java/com/mobileautomation/framework/locators/ProductDetailsLocators.java` | Added 3 raw accessibility-id string constants (`ADD_TO_CART_BUTTON_ACCESSIBILITY_ID`, `DECREASE_QUANTITY_ACCESSIBILITY_ID`, `COLOR_LIST_ACCESSIBILITY_ID`), exposed alongside their existing `By` constants — following the exact precedent already established by `PRODUCT_HIGHLIGHTS_RESOURCE_ID`/`PRODUCT_HIGHLIGHTS_LABEL` in the same file |
| `src/test/java/com/mobileautomation/framework/pages/ProductDetailsPage.java` | Added one `ScrollUtility.scrollToAccessibilityId(...)` call each to `addToCart()`, `isQuantitySelectorDisplayed()`, and `isColorSelectorDisplayed()`, before their existing interaction/check — no other line in either method changed |

No other file touched. No test class, no other Page Object, no locator outside `ProductDetailsLocators`, no config, no workflow YAML.

## Exact Changes

```diff
+    public static final String COLOR_LIST_ACCESSIBILITY_ID = "Displays available colors of selected product";
-    public static final By COLOR_LIST = AppiumBy.accessibilityId("Displays available colors of selected product");
+    public static final By COLOR_LIST = AppiumBy.accessibilityId(COLOR_LIST_ACCESSIBILITY_ID);
+    public static final String DECREASE_QUANTITY_ACCESSIBILITY_ID = "Decrease item quantity";
-    public static final By DECREASE_QUANTITY_BUTTON = AppiumBy.accessibilityId("Decrease item quantity");
+    public static final By DECREASE_QUANTITY_BUTTON = AppiumBy.accessibilityId(DECREASE_QUANTITY_ACCESSIBILITY_ID);
+    public static final String ADD_TO_CART_BUTTON_ACCESSIBILITY_ID = "Tap to add product to cart";
-    public static final By ADD_TO_CART_BUTTON = AppiumBy.accessibilityId("Tap to add product to cart");
+    public static final By ADD_TO_CART_BUTTON = AppiumBy.accessibilityId(ADD_TO_CART_BUTTON_ACCESSIBILITY_ID);
```

```diff
     public void addToCart() {
+        ScrollUtility.scrollToAccessibilityId(ProductDetailsLocators.ADD_TO_CART_BUTTON_ACCESSIBILITY_ID);
         elementActions.click(ProductDetailsLocators.ADD_TO_CART_BUTTON);
     }
```

```diff
     public boolean isQuantitySelectorDisplayed() {
+        ScrollUtility.scrollToAccessibilityId(ProductDetailsLocators.DECREASE_QUANTITY_ACCESSIBILITY_ID);
         return elementActions.isDisplayed(ProductDetailsLocators.DECREASE_QUANTITY_BUTTON)
                 && elementActions.isDisplayed(ProductDetailsLocators.QUANTITY_VALUE)
                 && elementActions.isDisplayed(ProductDetailsLocators.INCREASE_QUANTITY_BUTTON);
     }
```

```diff
     public boolean isColorSelectorDisplayed() {
+        ScrollUtility.scrollToAccessibilityId(ProductDetailsLocators.COLOR_LIST_ACCESSIBILITY_ID);
         return elementActions.isDisplayed(ProductDetailsLocators.COLOR_LIST);
     }
```

## Validation

- `./gradlew compileJava compileTestJava --no-daemon` — `BUILD SUCCESSFUL`, confirmed locally.
- No `Thread.sleep()`, no assertion weakened, no test removed or disabled, no locator value changed (all three `By` constants resolve to the exact same accessibility ids as before — only the raw string is now also exposed as a named constant).
- `ScrollUtility` itself was not modified — only its already-existing, already-proven `scrollToAccessibilityId` method is now called from three additional call sites.
- The scroll call is placed before each method's existing interaction/check, matching the exact pattern `scrollToHighlights()` already established successfully on this same screen (used by the already-passing `scrollToProductHighlights`, TC-013).

## Risk

**Low.** `UiScrollable.scrollIntoView(...)` (the mechanism behind `scrollToAccessibilityId`) is a no-op when its target is already visible — this is documented Android UiAutomator behavior, not custom logic this project wrote. On the real-device baseline, where these controls apparently render without scrolling, this change should be entirely unobservable. The only new behavior introduced is an additional scroll attempt immediately before three specific, already-existing interactions.

## Remaining Issues

This fix addresses only the 13 tests diagnosed in Phase 17.6A (11 `CartTest` methods + TC-010 + TC-011). Two failures remain, deliberately untouched, each with its own distinct root cause not yet investigated in this cycle:

- `ProductDetailsTest.addProductToCart` (TC-012) — fails earlier, at `ProductsPage.getCardPrice()` reading a product card's price via a parent-axis XPath, before navigation to Product Details even occurs. Unrelated to this fix.
- `NavigationTest.accessDrawerItems` (TC-028) — fails due to the emulator's lack of biometric hardware triggering an unexpected native dialog. Per Phase 17.5G, likely a "document, don't fix" case (Step 5), not yet formally classified.

Per Step 10, the next iteration should re-run CI to verify this fix's actual effect with real evidence before addressing either remaining issue.

---

**End of Document — Phase 17.6B Implementation Report, v1.0**
