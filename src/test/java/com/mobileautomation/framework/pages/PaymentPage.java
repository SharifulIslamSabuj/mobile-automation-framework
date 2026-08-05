package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.PaymentLocators;
import com.mobileautomation.framework.models.PaymentCard;
import com.mobileautomation.framework.utils.WaitUtility;

/**
 * Page Object for the Checkout — Payment screen (MA-LOC-001 §11). Added
 * Phase 12.4, narrowly scoped to what TC-022 requires: confirming arrival at
 * this screen via individual display checks for every static element —
 * mirrors exactly how {@code CheckoutPage} started narrow for TC-018/019
 * before growing data-entry methods for TC-020/021. Extended Phase 12.6 with
 * data entry, read-back, and a default-checked-state read for the Billing
 * checkbox (TC-023). Deliberately contains no checkbox <em>toggle</em> method
 * — Phase 12.5's duplicate-locator investigation of the toggled-off state is
 * already complete and no Test Case has needed to toggle it. Extended
 * Phase 13.2 with the Review Order navigation action (TC-026) —
 * {@link #tapReviewOrder()} mirrors {@code CheckoutPage#tapToPayment()}'s
 * established pattern exactly.
 */
public class PaymentPage extends BasePage {

    /**
     * @return whether the Payment screen has been reached — read via its
     * title, the same "screen-reached" signal {@code CheckoutPage#isDisplayed()}
     * uses for the Shipping Address screen. Kept separate from
     * {@link #isPaymentTitleDisplayed()} only in name, not behavior: the
     * former is this Page Object's screen-arrival check (used by navigation
     * helpers), the latter is TC-022's individual title-content assertion —
     * both read the same element, deliberately, so neither can drift from
     * the other.
     */
    public boolean isPaymentScreenDisplayed() {
        return isPaymentTitleDisplayed();
    }

    /** @return whether the Payment screen's title is displayed. */
    public boolean isPaymentTitleDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.PAYMENT_TITLE);
    }

    /** @return whether the "Enter a payment method" subtitle is displayed. */
    public boolean isPaymentSubtitleDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.PAYMENT_SUBTITLE);
    }

    /** @return whether the "You will not be charged..." details note is displayed. */
    public boolean isPaymentDetailsNoteDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.PAYMENT_DETAILS_NOTE);
    }

    /** @return whether the "Card" label is displayed. */
    public boolean isCardLabelDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.CARD_LABEL);
    }

    /** @return whether the Visa icon is displayed. */
    public boolean isVisaIconDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.VISA_ICON);
    }

    /** @return whether the Mastercard icon is displayed. */
    public boolean isMastercardIconDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.MASTERCARD_ICON);
    }

    /** @return whether the Cardholder Name field's label is displayed. */
    public boolean isCardholderNameLabelDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.CARDHOLDER_NAME_LABEL);
    }

    /** @return whether the Cardholder Name field is displayed. */
    public boolean isCardholderNameFieldDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.CARDHOLDER_NAME_FIELD);
    }

    /** @return whether the Card Number field's label is displayed. */
    public boolean isCardNumberLabelDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.CARD_NUMBER_LABEL);
    }

    /** @return whether the Card Number field (custom widget) is displayed. */
    public boolean isCardNumberFieldDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.CARD_NUMBER_FIELD);
    }

    /** @return whether the Expiration Date field's label is displayed. */
    public boolean isExpirationDateLabelDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.EXPIRATION_DATE_LABEL);
    }

    /** @return whether the Expiration Date field (custom widget) is displayed. */
    public boolean isExpirationDateFieldDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.EXPIRATION_DATE_FIELD);
    }

    /** @return whether the Security Code field's label is displayed. */
    public boolean isSecurityCodeLabelDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.SECURITY_CODE_LABEL);
    }

    /** @return whether the Security Code field is displayed. */
    public boolean isSecurityCodeFieldDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.SECURITY_CODE_FIELD);
    }

    /** @return whether the Billing-Same-As-Shipping checkbox is displayed. */
    public boolean isBillingCheckboxDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.BILLING_ADDRESS_CHECKBOX);
    }

    /** @return whether the Review Order button is displayed. */
    public boolean isReviewOrderButtonDisplayed() {
        return elementActions.isDisplayed(PaymentLocators.REVIEW_ORDER_BUTTON);
    }

    /**
     * @return whether every static Payment screen element is displayed —
     * composes the individual per-element checks above, mirroring
     * {@code CheckoutPage#areAllFieldsDisplayed()}'s composition pattern.
     * Retained only as an additional convenience; TC-022 asserts each
     * element individually rather than relying on this alone.
     */
    public boolean arePaymentScreenElementsDisplayed() {
        return isPaymentTitleDisplayed()
                && isPaymentSubtitleDisplayed()
                && isPaymentDetailsNoteDisplayed()
                && isCardLabelDisplayed()
                && isVisaIconDisplayed()
                && isMastercardIconDisplayed()
                && isCardholderNameLabelDisplayed()
                && isCardholderNameFieldDisplayed()
                && isCardNumberLabelDisplayed()
                && isCardNumberFieldDisplayed()
                && isExpirationDateLabelDisplayed()
                && isExpirationDateFieldDisplayed()
                && isSecurityCodeLabelDisplayed()
                && isSecurityCodeFieldDisplayed()
                && isBillingCheckboxDisplayed()
                && isReviewOrderButtonDisplayed();
    }

    /** Clears and re-enters the Cardholder Name field. Added Phase 12.6 for TC-023. */
    public void enterCardholderName(String value) {
        elementActions.clear(PaymentLocators.CARDHOLDER_NAME_FIELD);
        elementActions.type(PaymentLocators.CARDHOLDER_NAME_FIELD, value);
    }

    /**
     * Clears and re-enters the Card Number field. {@code value} is the raw
     * digit string to type — the custom widget auto-formats it with spaces
     * as you type (Phase 12.5, confirmed runtime evidence); use
     * {@link #getCardNumber()} afterward to read the formatted result back,
     * never the raw input.
     */
    public void enterCardNumber(String value) {
        elementActions.clear(PaymentLocators.CARD_NUMBER_FIELD);
        elementActions.type(PaymentLocators.CARD_NUMBER_FIELD, value);
    }

    /**
     * Clears and re-enters the Expiration Date field. {@code value} is the
     * raw digit string to type (e.g. {@code "1225"}) — the custom widget
     * auto-inserts the slash as you type (Phase 12.5, confirmed runtime
     * evidence), so {@link #getExpirationDate()} returns e.g. {@code "12/25"}.
     */
    public void enterExpirationDate(String value) {
        elementActions.clear(PaymentLocators.EXPIRATION_DATE_FIELD);
        elementActions.type(PaymentLocators.EXPIRATION_DATE_FIELD, value);
    }

    /** Clears and re-enters the Security Code field. Added Phase 12.6 for TC-023. */
    public void enterSecurityCode(String value) {
        elementActions.clear(PaymentLocators.SECURITY_CODE_FIELD);
        elementActions.type(PaymentLocators.SECURITY_CODE_FIELD, value);
    }

    /** Enters all four fields from {@code card} — composes the individual {@code enter*} methods above, mirroring {@code CheckoutPage#enterShippingAddress(ShippingAddress)}'s composition pattern. Added Phase 12.6 for TC-023. */
    public void enterPaymentCard(PaymentCard card) {
        enterCardholderName(card.cardholderName());
        enterCardNumber(card.cardNumber());
        enterExpirationDate(card.expirationDate());
        enterSecurityCode(card.securityCode());
    }

    /** @return the Cardholder Name field's current contents. Added Phase 12.6 for TC-023. */
    public String getCardholderName() {
        return elementActions.getText(PaymentLocators.CARDHOLDER_NAME_FIELD);
    }

    /** @return the Card Number field's current contents — the AUT's auto-formatted, space-grouped value (e.g. {@code "4111 1111 1111 1111"}), not the raw typed digits (Phase 12.5, confirmed runtime evidence). Added Phase 12.6 for TC-023. */
    public String getCardNumber() {
        return elementActions.getText(PaymentLocators.CARD_NUMBER_FIELD);
    }

    /** @return the Expiration Date field's current contents — the AUT's auto-formatted value with the slash inserted (e.g. {@code "12/25"}), not the raw typed digits (Phase 12.5, confirmed runtime evidence). Added Phase 12.6 for TC-023. */
    public String getExpirationDate() {
        return elementActions.getText(PaymentLocators.EXPIRATION_DATE_FIELD);
    }

    /** @return the Security Code field's current contents. Added Phase 12.6 for TC-023. */
    public String getSecurityCode() {
        return elementActions.getText(PaymentLocators.SECURITY_CODE_FIELD);
    }

    /**
     * @return whether the Billing-Same-As-Shipping checkbox is currently
     * checked — {@code true} by default (Phase 12.3/12.5, confirmed runtime
     * evidence). Reads the {@code checked} attribute directly rather than
     * {@code ElementActions.isSelected()}: Android's {@code Checkable.isChecked()}
     * and {@code View.isSelected()} are distinct view states, and
     * UiAutomator2 resolves {@code WebElement.isSelected()} to the latter —
     * confirmed by this method returning {@code false} for a visibly-checked
     * checkbox before this fix, discovered during this phase's own runtime
     * validation. Added Phase 12.6 for TC-023; no toggle method is provided,
     * since TC-023 only needs the default state and Phase 12.5 already
     * investigated the toggled-off (duplicate-locator) state directly.
     */
    public boolean isBillingCheckboxChecked() {
        return Boolean.parseBoolean(elementActions.getAttribute(PaymentLocators.BILLING_ADDRESS_CHECKBOX, "checked"));
    }

    /** @return whether the Review Order button is enabled (interactable). Added Phase 13.2 for TC-026. */
    public boolean isReviewOrderButtonEnabled() {
        return elementActions.isEnabled(PaymentLocators.REVIEW_ORDER_BUTTON);
    }

    /**
     * Taps the Review Order button, then waits for this screen's own title
     * to become invisible — mirrors {@code CheckoutPage#tapToPayment()}'s
     * established pattern exactly, confirming a real transition without
     * referencing anything on the destination (Review Order) screen. Added
     * Phase 13.2 for TC-026.
     *
     * @return {@code true} once the Payment title has become invisible.
     */
    public boolean tapReviewOrder() {
        elementActions.click(PaymentLocators.REVIEW_ORDER_BUTTON);
        return WaitUtility.waitForInvisibility(PaymentLocators.PAYMENT_TITLE);
    }
}
