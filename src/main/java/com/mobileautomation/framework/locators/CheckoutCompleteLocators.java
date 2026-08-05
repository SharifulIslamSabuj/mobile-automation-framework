package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Checkout Complete screen locators (MA-LOC-001 §13), source-decompiled from
 * {@code fragment_checkout_complete.xml} — none is invented. Added Phase 13.2
 * for TC-026: title, all three confirmation messages, and the Continue
 * Shopping action.
 * <p>
 * {@code CONTINUE_SHOPPING_BUTTON}'s underlying resource-id is misspelled in
 * source ({@code shoopingBt}, not {@code shoppingBt}) — the accessibility id
 * is used instead, both for the same reuse-discipline every other
 * checkout-flow button locator follows, and to avoid depending on a
 * source-level typo.
 */
public final class CheckoutCompleteLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By CHECKOUT_COMPLETE_TITLE = By.id(PACKAGE + "completeTV");
    public static final By THANK_YOU_MESSAGE = By.id(PACKAGE + "thankYouTV");
    public static final By SUCCESS_MESSAGE = By.id(PACKAGE + "swagTV");
    public static final By DISPATCH_MESSAGE = By.id(PACKAGE + "orderTV");

    /** Accessibility id — underlying resource-id is misspelled in source ({@code shoopingBt}); see class Javadoc. */
    public static final By CONTINUE_SHOPPING_BUTTON = AppiumBy.accessibilityId("Tap to open catalog");

    private CheckoutCompleteLocators() {
    }
}
