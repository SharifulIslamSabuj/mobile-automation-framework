package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.CheckoutLocators;
import com.mobileautomation.framework.models.ShippingAddress;
import com.mobileautomation.framework.utils.WaitUtility;

/**
 * Page Object for the Checkout — Shipping Address screen (MA-LOC-001 §10).
 * <p>
 * Added Phase 11.8, narrowly scoped to what TC-018 required: confirming
 * arrival at this screen (title, subtitle, all seven field labels
 * displayed). Extended Phase 12.2 with individual per-field display checks
 * (TC-019), data entry and read-back (TC-020), and the "To Payment" action
 * (TC-021). The Payment screen itself (TC-022/TC-023) remains out of scope,
 * deliberately deferred — mirrors exactly how {@code CartPage} grew
 * incrementally per Test Case starting Phase 9.5B.
 * <p>
 * <b>{@link #tapToPayment()} note:</b> waits for this screen's own title to
 * become invisible (proving a real transition occurred) rather than
 * referencing anything on the Payment screen — no Payment-screen locator
 * exists anywhere in this codebase yet, and this method deliberately does
 * not add one, per this phase's explicit "do not validate the Payment
 * screen itself" instruction. Uses {@code WaitUtility.waitForInvisibility(By)}
 * directly, the same accepted precedent {@code CartPage.removeItem()}
 * established in Phase 11.4.
 */
public class CheckoutPage extends BasePage {

    /** @return whether the Shipping Address screen's title is displayed. */
    public boolean isDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.SHIPPING_TITLE);
    }

    /** @return whether the "Enter a shipping address" subtitle is displayed. */
    public boolean isSubtitleDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.SHIPPING_SUBTITLE);
    }

    /** @return whether the Full Name field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isFullNameFieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.FULL_NAME_FIELD);
    }

    /** @return whether the Address Line 1 field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isAddressLine1FieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.ADDRESS_LINE_1_FIELD);
    }

    /** @return whether the Address Line 2 field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isAddressLine2FieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.ADDRESS_LINE_2_FIELD);
    }

    /** @return whether the City field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isCityFieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.CITY_FIELD);
    }

    /** @return whether the State/Region field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isStateFieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.STATE_FIELD);
    }

    /** @return whether the Zip Code field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isZipCodeFieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.ZIP_CODE_FIELD);
    }

    /** @return whether the Country field is displayed. Added Phase 12.2 for TC-019. */
    public boolean isCountryFieldDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.COUNTRY_FIELD);
    }

    /**
     * @return whether all seven Shipping Address fields are displayed —
     * composes the individual per-field checks above (added Phase 12.2;
     * this method itself unchanged in behavior/signature since Phase 11.8,
     * only its implementation was refactored to avoid duplicating the
     * per-field {@code elementActions.isDisplayed} calls).
     */
    public boolean areAllFieldsDisplayed() {
        return isFullNameFieldDisplayed()
                && isAddressLine1FieldDisplayed()
                && isAddressLine2FieldDisplayed()
                && isCityFieldDisplayed()
                && isStateFieldDisplayed()
                && isZipCodeFieldDisplayed()
                && isCountryFieldDisplayed();
    }

    /** Clears and re-enters the Full Name field. Added Phase 12.2 for TC-020. */
    public void enterFullName(String value) {
        elementActions.clear(CheckoutLocators.FULL_NAME_FIELD);
        elementActions.type(CheckoutLocators.FULL_NAME_FIELD, value);
    }

    /** Clears and re-enters the Address Line 1 field. Added Phase 12.2 for TC-020. */
    public void enterAddressLine1(String value) {
        elementActions.clear(CheckoutLocators.ADDRESS_LINE_1_FIELD);
        elementActions.type(CheckoutLocators.ADDRESS_LINE_1_FIELD, value);
    }

    /** Clears and re-enters the Address Line 2 field. Added Phase 12.2 for TC-020. */
    public void enterAddressLine2(String value) {
        elementActions.clear(CheckoutLocators.ADDRESS_LINE_2_FIELD);
        elementActions.type(CheckoutLocators.ADDRESS_LINE_2_FIELD, value);
    }

    /** Clears and re-enters the City field. Added Phase 12.2 for TC-020. */
    public void enterCity(String value) {
        elementActions.clear(CheckoutLocators.CITY_FIELD);
        elementActions.type(CheckoutLocators.CITY_FIELD, value);
    }

    /** Clears and re-enters the State/Region field. Added Phase 12.2 for TC-020. */
    public void enterState(String value) {
        elementActions.clear(CheckoutLocators.STATE_FIELD);
        elementActions.type(CheckoutLocators.STATE_FIELD, value);
    }

    /** Clears and re-enters the Zip Code field. Added Phase 12.2 for TC-020. */
    public void enterZipCode(String value) {
        elementActions.clear(CheckoutLocators.ZIP_CODE_FIELD);
        elementActions.type(CheckoutLocators.ZIP_CODE_FIELD, value);
    }

    /** Clears and re-enters the Country field. Added Phase 12.2 for TC-020. */
    public void enterCountry(String value) {
        elementActions.clear(CheckoutLocators.COUNTRY_FIELD);
        elementActions.type(CheckoutLocators.COUNTRY_FIELD, value);
    }

    /** Enters all seven fields from {@code address} — composes the individual {@code enter*} methods above, mirroring {@code LoginPage#login(String, String)}'s composition pattern. Added Phase 12.2 for TC-020. */
    public void enterShippingAddress(ShippingAddress address) {
        enterFullName(address.fullName());
        enterAddressLine1(address.addressLine1());
        enterAddressLine2(address.addressLine2());
        enterCity(address.city());
        enterState(address.state());
        enterZipCode(address.zipCode());
        enterCountry(address.country());
    }

    /** @return the Full Name field's current contents. Added Phase 12.2 for TC-020. */
    public String getFullName() {
        return elementActions.getText(CheckoutLocators.FULL_NAME_FIELD);
    }

    /** @return the Address Line 1 field's current contents. Added Phase 12.2 for TC-020. */
    public String getAddressLine1() {
        return elementActions.getText(CheckoutLocators.ADDRESS_LINE_1_FIELD);
    }

    /** @return the Address Line 2 field's current contents. Added Phase 12.2 for TC-020. */
    public String getAddressLine2() {
        return elementActions.getText(CheckoutLocators.ADDRESS_LINE_2_FIELD);
    }

    /** @return the City field's current contents. Added Phase 12.2 for TC-020. */
    public String getCity() {
        return elementActions.getText(CheckoutLocators.CITY_FIELD);
    }

    /** @return the State/Region field's current contents. Added Phase 12.2 for TC-020. */
    public String getState() {
        return elementActions.getText(CheckoutLocators.STATE_FIELD);
    }

    /** @return the Zip Code field's current contents. Added Phase 12.2 for TC-020. */
    public String getZipCode() {
        return elementActions.getText(CheckoutLocators.ZIP_CODE_FIELD);
    }

    /** @return the Country field's current contents. Added Phase 12.2 for TC-020. */
    public String getCountry() {
        return elementActions.getText(CheckoutLocators.COUNTRY_FIELD);
    }

    /** @return whether the To Payment button is displayed. Added Phase 12.2 for TC-021. */
    public boolean isToPaymentButtonDisplayed() {
        return elementActions.isDisplayed(CheckoutLocators.TO_PAYMENT_BUTTON);
    }

    /** @return whether the To Payment button is enabled (interactable). Added Phase 12.2 for TC-021. */
    public boolean isToPaymentButtonEnabled() {
        return elementActions.isEnabled(CheckoutLocators.TO_PAYMENT_BUTTON);
    }

    /**
     * Taps the To Payment button, then waits for this screen's own title to
     * become invisible — a real UI condition confirming a genuine
     * transition occurred, without referencing anything on the destination
     * (Payment) screen. See class Javadoc for why. Returns the wait's own
     * result directly (rather than requiring the caller to re-query
     * {@link #isDisplayed()} afterward, which would otherwise re-pay a full
     * explicit-wait timeout for a condition already known — the same
     * inefficiency disclosed in Phase 11.4/11.5 for {@code CartPage}'s
     * {@code verifyHidden} checks, avoided here). Added Phase 12.2 for TC-021.
     *
     * @return {@code true} once the Shipping Address title has become invisible.
     */
    public boolean tapToPayment() {
        elementActions.click(CheckoutLocators.TO_PAYMENT_BUTTON);
        return WaitUtility.waitForInvisibility(CheckoutLocators.SHIPPING_TITLE);
    }
}
