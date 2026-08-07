package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Product Details screen locators. Every value is quoted directly from
 * MA-LOC-001 §8 (Locator Repository) — none is invented, none is duplicated
 * elsewhere. Color swatch accessibility ids are dynamically constructed at
 * runtime by the AUT itself as {@code "<ColorName> color"} (MA-LOC-001 §19.4)
 * — {@code colorSwatch(String)} below applies that documented, confirmed
 * pattern rather than hardcoding any specific color.
 */
public final class ProductDetailsLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By PRODUCT_IMAGE = AppiumBy.accessibilityId("Displays selected product");
    public static final By PRODUCT_NAME = By.id(PACKAGE + "productTV");
    public static final By PRODUCT_PRICE = By.id(PACKAGE + "priceTV");
    /** Raw accessibility id — exposed separately (same reason as {@link #PRODUCT_HIGHLIGHTS_RESOURCE_ID}) because {@code ScrollUtility#scrollToAccessibilityId(String)} needs the raw id, not a {@link By}. Added Phase 17.6A: on some viewports (confirmed via CI emulator evidence) this element renders below the initially-visible screen area. */
    public static final String COLOR_LIST_ACCESSIBILITY_ID = "Displays available colors of selected product";
    public static final By COLOR_LIST = AppiumBy.accessibilityId(COLOR_LIST_ACCESSIBILITY_ID);
    /** Raw accessibility id — same reason as {@link #COLOR_LIST_ACCESSIBILITY_ID} above. Added Phase 17.6A. */
    public static final String DECREASE_QUANTITY_ACCESSIBILITY_ID = "Decrease item quantity";
    public static final By DECREASE_QUANTITY_BUTTON = AppiumBy.accessibilityId(DECREASE_QUANTITY_ACCESSIBILITY_ID);
    public static final By QUANTITY_VALUE = By.id(PACKAGE + "noTV");
    public static final By INCREASE_QUANTITY_BUTTON = AppiumBy.accessibilityId("Increase item quantity");
    /** Raw accessibility id — same reason as {@link #COLOR_LIST_ACCESSIBILITY_ID} above. Added Phase 17.6A. */
    public static final String ADD_TO_CART_BUTTON_ACCESSIBILITY_ID = "Tap to add product to cart";
    public static final By ADD_TO_CART_BUTTON = AppiumBy.accessibilityId(ADD_TO_CART_BUTTON_ACCESSIBILITY_ID);

    /** Raw resource-id string for the Product Highlights label — exposed separately (not just wrapped in {@link #PRODUCT_HIGHLIGHTS_LABEL}) because {@code ScrollUtility#scrollToResourceId(String)} needs the raw id, not a {@link By}. Added Phase 10.3 for TC-013. */
    public static final String PRODUCT_HIGHLIGHTS_RESOURCE_ID = PACKAGE + "productHeightLightsTV"; // sic — verbatim from source, MA-LOC-001 §8
    public static final By PRODUCT_HIGHLIGHTS_LABEL = By.id(PRODUCT_HIGHLIGHTS_RESOURCE_ID);
    public static final By PRODUCT_DESCRIPTION = By.id(PACKAGE + "descTV");

    /** The currently-rendered color swatch image, found by resource-id — unlike {@link #colorSwatch(String)}, does not require knowing the color name in advance. Added Phase 9.5B: TC-012 Step 11 verifies the swatch's actual {@code content-desc} value (e.g. {@code "Unknown color"}), which is exactly what's being read, not assumed. */
    public static final By COLOR_SWATCH_IMAGE = By.id(PACKAGE + "colorIV");

    private ProductDetailsLocators() {
    }

    /** @param colorName e.g. {@code "Black"}, {@code "Green"}, {@code "Gray"}, {@code "Blue"} — applies the AUT's own dynamic content-desc pattern, does not invent a new locator strategy. */
    public static By colorSwatch(String colorName) {
        return AppiumBy.accessibilityId(colorName + " color");
    }
}
