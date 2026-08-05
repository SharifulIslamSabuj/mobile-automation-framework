package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Review Order screen locators (MA-LOC-001 §12), source-decompiled from
 * {@code fragment_place_order.xml} — none is invented. Added Phase 13.2,
 * narrowly scoped to what TC-026 requires: one confirmed anchor element per
 * business section, plus the Place Order action.
 * <p>
 * {@code CHECKOUT_TITLE}/{@code REVIEW_ORDER_SUBTITLE} reuse the exact same
 * resource-ids as the Shipping Address screen's own title/subtitle
 * ({@code checkoutTitleTV}/{@code enterShippingAddressTV}) — safe here
 * because only one screen is ever active at a time, the same reasoning
 * already established by {@code PaymentLocators}/{@code CheckoutLocators}.
 * <p>
 * Each business section is verified via its own confirmed, uniquely-scoped,
 * resource-id-based element rather than this screen's text-only, no-id
 * section labels ("Deliver Address"/"Payment Method", MA-LOC-001 §12,
 * Priority 4 — no {@code android:id} exists to select them by) — a
 * deliberate choice to avoid introducing a new text-based locator strategy
 * this codebase has not needed until now. {@code billingAddressTV} ("Billing
 * address is the same as shipping address") is the only Billing Address
 * content this flow can ever show, since the Payment screen's Billing
 * checkbox is confirmed checked by default (Phase 12.3/12.5) and this flow
 * never unchecks it. {@code dhlTV} ("DHL Standard Delivery") is confirmed in
 * source as the only shipping-method row — no selector/alternative exists.
 * {@code paymentBtn} is reused a fourth time for Place Order — accessibility
 * id used, never the bare resource-id, the same discipline every prior
 * checkout-flow locator class follows.
 * <p>
 * <b>Scrolling (discovered Phase 13.2 runtime validation):</b> this screen
 * is a single scrollable page taller than the viewport — Product
 * information and Delivery Address render within the initial viewport, but
 * Payment Method, Billing Address, Shipping Method, Total, and Place Order
 * render below the fold. The {@code *_RESOURCE_ID} String constants below
 * (alongside their {@code By} counterparts) exist for {@code ReviewOrderPage}
 * to pass to the existing, unmodified {@code ScrollUtility#scrollToResourceId(String)}
 * (Phase 5) before checking each below-the-fold element — the same reuse
 * pattern {@code ProductDetailsPage#scrollToHighlights()} already
 * established. This is a real AUT layout characteristic, not a defect.
 */
public final class ReviewOrderLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By CHECKOUT_TITLE = By.id(PACKAGE + "checkoutTitleTV");
    public static final By REVIEW_ORDER_SUBTITLE = By.id(PACKAGE + "enterShippingAddressTV");

    /** Order item Product Name, repeats per RecyclerView row. */
    public static final By ORDER_ITEM_NAME = By.id(PACKAGE + "titleTV");
    /** Order item Product Price, repeats per RecyclerView row. */
    public static final By ORDER_ITEM_PRICE = By.id(PACKAGE + "priceTV");

    /** Delivery Address section anchor — Full Name from the Shipping summary. */
    public static final By DELIVERY_ADDRESS_FULL_NAME = By.id(PACKAGE + "fullNameTV");

    /** Payment Method section anchor — Cardholder Name from the Payment summary. Below the fold — see class Javadoc. */
    public static final String PAYMENT_METHOD_CARDHOLDER_NAME_RESOURCE_ID = PACKAGE + "cardHolderTV";
    public static final By PAYMENT_METHOD_CARDHOLDER_NAME = By.id(PAYMENT_METHOD_CARDHOLDER_NAME_RESOURCE_ID);

    /** Billing Address section anchor — the "same as shipping" note, the only Billing Address content this flow shows. Below the fold — see class Javadoc. */
    public static final String BILLING_ADDRESS_SAME_NOTE_RESOURCE_ID = PACKAGE + "billingAddressTV";
    public static final By BILLING_ADDRESS_SAME_NOTE = By.id(BILLING_ADDRESS_SAME_NOTE_RESOURCE_ID);

    /** Shipping Method section anchor — "DHL Standard Delivery", the only shipping-method row confirmed in source. Below the fold — see class Javadoc. */
    public static final String SHIPPING_METHOD_LABEL_RESOURCE_ID = PACKAGE + "dhlTV";
    public static final By SHIPPING_METHOD_LABEL = By.id(SHIPPING_METHOD_LABEL_RESOURCE_ID);

    public static final String TOTAL_ITEMS_COUNT_RESOURCE_ID = PACKAGE + "itemNumberTV";
    public static final By TOTAL_ITEMS_COUNT = By.id(TOTAL_ITEMS_COUNT_RESOURCE_ID);
    public static final String TOTAL_AMOUNT_RESOURCE_ID = PACKAGE + "totalAmountTV";
    public static final By TOTAL_AMOUNT = By.id(TOTAL_AMOUNT_RESOURCE_ID);

    /** Accessibility id — fourth reuse of resource-id {@code paymentBtn} across the checkout flow; never the bare resource-id. Below the fold — see class Javadoc. */
    public static final String PLACE_ORDER_BUTTON_ACCESSIBILITY_ID = "Completes the process of checkout";
    public static final By PLACE_ORDER_BUTTON = AppiumBy.accessibilityId(PLACE_ORDER_BUTTON_ACCESSIBILITY_ID);

    private ReviewOrderLocators() {
    }
}
