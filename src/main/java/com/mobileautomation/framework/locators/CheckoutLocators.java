package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Checkout — Shipping Address screen locators. Every value is quoted
 * directly from MA-LOC-001 §10 (Locator Repository), source-confirmed
 * against {@code fragment_checkout_info.xml} — none is invented. Added
 * Phase 11.8, narrowly scoped to what TC-018 requires: confirming arrival at
 * this screen (title, subtitle, all seven field labels displayed). Extended
 * Phase 12.2 with the "To Payment" button (TC-021). Field data entry
 * locators (TC-020) were already present as of Phase 11.8 (needed to detect
 * field presence) and are now also used for data entry. The Payment screen
 * itself (TC-022/023) remains out of scope, deliberately deferred — the same
 * narrow-then-grow pattern {@code CartLocators} followed starting Phase 9.5B.
 */
public final class CheckoutLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By SHIPPING_TITLE = By.id(PACKAGE + "checkoutTitleTV");
    public static final By SHIPPING_SUBTITLE = By.id(PACKAGE + "enterShippingAddressTV");
    public static final By FULL_NAME_FIELD = By.id(PACKAGE + "fullNameET");
    public static final By ADDRESS_LINE_1_FIELD = By.id(PACKAGE + "address1ET");
    public static final By ADDRESS_LINE_2_FIELD = By.id(PACKAGE + "address2ET");
    public static final By CITY_FIELD = By.id(PACKAGE + "cityET");
    public static final By STATE_FIELD = By.id(PACKAGE + "stateET");
    public static final By ZIP_CODE_FIELD = By.id(PACKAGE + "zipET");
    public static final By COUNTRY_FIELD = By.id(PACKAGE + "countryET");

    /** Raw accessibility id — exposed separately because {@code ScrollUtility#scrollToAccessibilityId(String)} needs the raw id, not a {@link By} (same precedent as {@code ProductDetailsLocators}, Phase 17.6A). Added Phase 17.6C: on some viewports (CI emulator evidence) this button renders below the fold once the shipping form is populated. */
    public static final String TO_PAYMENT_BUTTON_ACCESSIBILITY_ID = "Saves user info for checkout";
    /** To Payment / Continue button — accessibility id, never the reused resource-id {@code paymentBtn} (LOCATOR_REPOSITORY.md line 214: {@code paymentBtn} is reused 3 times across this screen, the Payment screen, and the Review Order flow — accessibility id is the reliable differentiator). Added Phase 12.2 for TC-021. */
    public static final By TO_PAYMENT_BUTTON = AppiumBy.accessibilityId(TO_PAYMENT_BUTTON_ACCESSIBILITY_ID);

    private CheckoutLocators() {
    }
}
