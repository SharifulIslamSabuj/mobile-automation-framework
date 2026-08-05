package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.ReviewOrderLocators;
import com.mobileautomation.framework.utils.ScrollUtility;
import com.mobileautomation.framework.utils.WaitUtility;

/**
 * Page Object for the Review Order screen (MA-LOC-001 §12). Added Phase 13.2,
 * narrowly scoped to what TC-026 requires: individual display verification
 * of every business section (Checkout title, "Review your order" subtitle,
 * Product information, Delivery Address, Payment Method, Billing Address,
 * Shipping Method, Total) plus the Place Order action — mirrors exactly how
 * {@code CheckoutPage}/{@code PaymentPage} started narrow for their own
 * first Test Case.
 * <p>
 * <b>Single-item assumption:</b> {@link #getFirstProductName()} and
 * {@link #getFirstProductPrice()} each read index 0 of independently
 * resolved lists — safe only because this flow guarantees exactly one item
 * in the order, the same assumption {@code CartPage#getFirstItemName()}/
 * {@code #getFirstItemPrice()} already document for their own equivalent
 * methods. Must not be generalized to a multi-item order.
 * <p>
 * <b>Scrolling:</b> this screen is a single scrollable page taller than the
 * viewport — Product information and Delivery Address render within the
 * initial viewport, but Payment Method, Billing Address, Shipping Method,
 * Total, and Place Order render below the fold (discovered Phase 13.2
 * runtime validation — a real AUT layout characteristic, not a defect).
 * Each below-the-fold accessor scrolls to its own target first via the
 * existing, unmodified {@code ScrollUtility#scrollToResourceId(String)}/
 * {@code #scrollToAccessibilityId(String)} — the same reuse pattern
 * {@code ProductDetailsPage#scrollToHighlights()} already established;
 * safe to call even if the target is already in view.
 */
public class ReviewOrderPage extends BasePage {

    /** @return whether the "Checkout" title is displayed. */
    public boolean isCheckoutTitleDisplayed() {
        return elementActions.isDisplayed(ReviewOrderLocators.CHECKOUT_TITLE);
    }

    /** @return whether the "Review your order" subtitle is displayed. */
    public boolean isReviewOrderSubtitleDisplayed() {
        return elementActions.isDisplayed(ReviewOrderLocators.REVIEW_ORDER_SUBTITLE);
    }

    /** @return the first (and, for this flow's single-item order, only) order item's product name. */
    public String getFirstProductName() {
        return elementActions.findAll(ReviewOrderLocators.ORDER_ITEM_NAME).get(0).getText();
    }

    /** @return the first (and, for this flow's single-item order, only) order item's product price. */
    public String getFirstProductPrice() {
        return elementActions.findAll(ReviewOrderLocators.ORDER_ITEM_PRICE).get(0).getText();
    }

    /** @return whether the Delivery Address section is displayed (anchored via its Full Name field). */
    public boolean isDeliveryAddressDisplayed() {
        return elementActions.isDisplayed(ReviewOrderLocators.DELIVERY_ADDRESS_FULL_NAME);
    }

    /** @return the Delivery Address section's displayed Full Name. */
    public String getDeliveryAddressFullName() {
        return elementActions.getText(ReviewOrderLocators.DELIVERY_ADDRESS_FULL_NAME);
    }

    /** @return whether the Payment Method section is displayed (anchored via its Cardholder Name field). Scrolls to it first — below the fold, see class Javadoc. */
    public boolean isPaymentMethodDisplayed() {
        ScrollUtility.scrollToResourceId(ReviewOrderLocators.PAYMENT_METHOD_CARDHOLDER_NAME_RESOURCE_ID);
        return elementActions.isDisplayed(ReviewOrderLocators.PAYMENT_METHOD_CARDHOLDER_NAME);
    }

    /** @return the Payment Method section's displayed Cardholder Name. */
    public String getPaymentMethodCardholderName() {
        return elementActions.getText(ReviewOrderLocators.PAYMENT_METHOD_CARDHOLDER_NAME);
    }

    /** @return whether the Billing Address section is displayed — the "same as shipping" note, the only Billing Address content this flow shows (Billing checkbox is checked by default and never unchecked here). Scrolls to it first — below the fold, see class Javadoc. */
    public boolean isBillingAddressSameNoteDisplayed() {
        ScrollUtility.scrollToResourceId(ReviewOrderLocators.BILLING_ADDRESS_SAME_NOTE_RESOURCE_ID);
        return elementActions.isDisplayed(ReviewOrderLocators.BILLING_ADDRESS_SAME_NOTE);
    }

    /** @return whether the Shipping Method section is displayed ("DHL Standard Delivery" — the only shipping-method row confirmed in source). Scrolls to it first — below the fold, see class Javadoc. */
    public boolean isShippingMethodDisplayed() {
        ScrollUtility.scrollToResourceId(ReviewOrderLocators.SHIPPING_METHOD_LABEL_RESOURCE_ID);
        return elementActions.isDisplayed(ReviewOrderLocators.SHIPPING_METHOD_LABEL);
    }

    /** @return whether the Total Items Count is displayed. Scrolls to it first — below the fold, see class Javadoc. */
    public boolean isTotalItemsCountDisplayed() {
        ScrollUtility.scrollToResourceId(ReviewOrderLocators.TOTAL_ITEMS_COUNT_RESOURCE_ID);
        return elementActions.isDisplayed(ReviewOrderLocators.TOTAL_ITEMS_COUNT);
    }

    /** @return whether the Total Amount is displayed. Scrolls to it first — below the fold, see class Javadoc. */
    public boolean isTotalAmountDisplayed() {
        ScrollUtility.scrollToResourceId(ReviewOrderLocators.TOTAL_AMOUNT_RESOURCE_ID);
        return elementActions.isDisplayed(ReviewOrderLocators.TOTAL_AMOUNT);
    }

    /** @return whether the Place Order button is displayed. Scrolls to it first — below the fold, see class Javadoc. */
    public boolean isPlaceOrderButtonDisplayed() {
        ScrollUtility.scrollToAccessibilityId(ReviewOrderLocators.PLACE_ORDER_BUTTON_ACCESSIBILITY_ID);
        return elementActions.isDisplayed(ReviewOrderLocators.PLACE_ORDER_BUTTON);
    }

    /** @return whether the Place Order button is enabled (interactable). */
    public boolean isPlaceOrderButtonEnabled() {
        return elementActions.isEnabled(ReviewOrderLocators.PLACE_ORDER_BUTTON);
    }

    /**
     * Taps Place Order, then waits for this screen's own title to become
     * invisible — mirrors {@code CheckoutPage#tapToPayment()}'s established
     * pattern exactly, confirming a real transition without referencing
     * anything on the destination (Checkout Complete) screen.
     *
     * @return {@code true} once the Checkout title has become invisible.
     */
    public boolean tapPlaceOrder() {
        elementActions.click(ReviewOrderLocators.PLACE_ORDER_BUTTON);
        return WaitUtility.waitForInvisibility(ReviewOrderLocators.CHECKOUT_TITLE);
    }
}
