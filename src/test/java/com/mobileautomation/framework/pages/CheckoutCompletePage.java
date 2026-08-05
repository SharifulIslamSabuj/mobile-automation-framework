package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.CheckoutCompleteLocators;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Page Object for the Checkout Complete screen (MA-LOC-001 §13). Added
 * Phase 13.2 for TC-026: individual display verification of the title and
 * all three confirmation messages, plus the Continue Shopping action.
 * <p>
 * Deliberately does not assert on the header cart badge here — MA-LOC-001
 * §13's own confirmed evidence is that the badge does <b>not</b> reset on
 * this screen (persists showing the pre-checkout count); that assertion
 * belongs to the Products screen after Continue Shopping is tapped, not
 * this Page Object.
 * <p>
 * <b>Google Pay "Save card to Google?" dialog (discovered Phase 13.2
 * runtime validation):</b> after a real card number is submitted through
 * Place Order ("Completes the process of checkout"), Android's Google Play
 * Services layer — not the AUT — surfaces a system-level "Save card to
 * Google?" prompt, observed appearing after {@link #tapContinueShopping()}
 * and obscuring the Products screen underneath it. This is the same class
 * of device-level interference Phase 12.5 found for the Card Number field's
 * "Scan new card" Autofill suggestion — external to the AUT, not a defect
 * in it. {@link #tapContinueShopping()} dismisses it via "Not now" if
 * present, located with {@code AppiumBy.androidUIAutomator(...)}, the exact
 * same locator strategy {@code ScrollUtility} already uses elsewhere in
 * this framework — no new locator strategy, no framework modification.
 */
public class CheckoutCompletePage extends BasePage {

    /** Google Play Services' "Save card to Google?" dialog's dismiss button — a device-level system dialog, not an AUT element; see class Javadoc. */
    private static final By SAVE_CARD_DIALOG_NOT_NOW_BUTTON =
            AppiumBy.androidUIAutomator("new UiSelector().text(\"Not now\")");

    /** @return whether the "Checkout Complete" title is displayed. */
    public boolean isCheckoutCompleteTitleDisplayed() {
        return elementActions.isDisplayed(CheckoutCompleteLocators.CHECKOUT_COMPLETE_TITLE);
    }

    /** @return whether the "Thank you for your order" message is displayed. */
    public boolean isThankYouMessageDisplayed() {
        return elementActions.isDisplayed(CheckoutCompleteLocators.THANK_YOU_MESSAGE);
    }

    /** @return whether the "Your new swag is on its way" message is displayed. */
    public boolean isSuccessMessageDisplayed() {
        return elementActions.isDisplayed(CheckoutCompleteLocators.SUCCESS_MESSAGE);
    }

    /** @return whether the order-dispatched message is displayed. */
    public boolean isDispatchMessageDisplayed() {
        return elementActions.isDisplayed(CheckoutCompleteLocators.DISPATCH_MESSAGE);
    }

    /** @return whether the Continue Shopping button is displayed. */
    public boolean isContinueShoppingButtonDisplayed() {
        return elementActions.isDisplayed(CheckoutCompleteLocators.CONTINUE_SHOPPING_BUTTON);
    }

    /** @return whether the Continue Shopping button is enabled (interactable). */
    public boolean isContinueShoppingButtonEnabled() {
        return elementActions.isEnabled(CheckoutCompleteLocators.CONTINUE_SHOPPING_BUTTON);
    }

    /**
     * Taps Continue Shopping, then dismisses Google Play Services' "Save
     * card to Google?" dialog if it appears — see class Javadoc. Does not
     * itself wait for or reference the Products screen — mirrors
     * {@code CartPage#tapProceedToCheckout()}'s pattern (the caller
     * determines and asserts the destination); here there is only one
     * possible destination, so the caller's own {@code ProductsPage.isDisplayed()}
     * check is sufficient.
     */
    public void tapContinueShopping() {
        elementActions.click(CheckoutCompleteLocators.CONTINUE_SHOPPING_BUTTON);
        if (elementActions.isDisplayed(SAVE_CARD_DIALOG_NOT_NOW_BUTTON)) {
            elementActions.click(SAVE_CARD_DIALOG_NOT_NOW_BUTTON);
        }
    }
}
