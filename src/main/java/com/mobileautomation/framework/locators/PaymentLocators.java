package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Checkout — Payment screen locators (MA-LOC-001 §11). Every resource-id and
 * accessibility-id value is quoted directly from the Locator Repository and
 * independently re-confirmed against real-device evidence collected in
 * Phase 12.3 ({@code p16_payment_dump.xml}) — none is invented. Added
 * Phase 12.4, narrowly scoped to what TC-022 requires: confirming arrival at
 * this screen (every static element individually displayed). Data-entry
 * locators for the two custom widgets ({@code cardNumberET},
 * {@code expirationDateET}) and {@code securityCodeET} are included only for
 * display verification — TC-022 does not type into them, per Phase 12.3's
 * unresolved custom-widget-compatibility risk (MA-LOC-001 §11); that
 * remains TC-023's concern.
 * <p>
 * {@code paymentBtn} is reused three times across the Shipping screen, this
 * screen, and the Review Order flow (MA-LOC-001 §10/§11) — accessibility id
 * is used for {@link #REVIEW_ORDER_BUTTON}, never the bare resource-id, the
 * same discipline {@code CheckoutLocators.TO_PAYMENT_BUTTON} already
 * follows. {@code nameTV}/{@code cardNumberTV}/{@code expirationDateTV}/
 * {@code securityCodeTV} are screen-scoped per MA-LOC-001 §14.3's
 * disambiguation rule — safe as bare resource-ids here because they are
 * only ever queried while this screen is the active one.
 */
public final class PaymentLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By PAYMENT_TITLE = By.id(PACKAGE + "enterPaymentTitleTV");
    public static final By PAYMENT_SUBTITLE = By.id(PACKAGE + "enterPaymentMethodTV");
    public static final By PAYMENT_DETAILS_NOTE = By.id(PACKAGE + "paymentDetailsTV");
    public static final By CARD_LABEL = By.id(PACKAGE + "cardTV");

    /** Accessibility id — MA-LOC-001 §11 primary locator; alternative resource-id {@code visaIV}. Not confirmed clickable in source (decorative icon). */
    public static final By VISA_ICON = AppiumBy.accessibilityId("Visa card");

    /** Accessibility id — MA-LOC-001 §11 primary locator; alternative resource-id {@code mastercardIV}. Not confirmed clickable in source (decorative icon). */
    public static final By MASTERCARD_ICON = AppiumBy.accessibilityId("Mastercard");

    public static final By CARDHOLDER_NAME_LABEL = By.id(PACKAGE + "nameTV");
    public static final By CARDHOLDER_NAME_FIELD = By.id(PACKAGE + "nameET");
    public static final By CARD_NUMBER_LABEL = By.id(PACKAGE + "cardNumberTV");

    /** Custom widget {@code com.uphyca.creditcardedittext.CreditCardNumberEditText} (MA-LOC-001 §11) — reports as {@code android.widget.EditText} in the accessibility tree (Phase 12.3 runtime evidence). Display-verification only; not typed into. */
    public static final By CARD_NUMBER_FIELD = By.id(PACKAGE + "cardNumberET");

    public static final By EXPIRATION_DATE_LABEL = By.id(PACKAGE + "expirationDateTV");

    /** Custom widget {@code com.uphyca.creditcardedittext.CreditCardDateEditText} (MA-LOC-001 §11) — same accessibility-class caveat as {@link #CARD_NUMBER_FIELD}. Display-verification only; not typed into. */
    public static final By EXPIRATION_DATE_FIELD = By.id(PACKAGE + "expirationDateET");

    public static final By SECURITY_CODE_LABEL = By.id(PACKAGE + "securityCodeTV");
    public static final By SECURITY_CODE_FIELD = By.id(PACKAGE + "securityCodeET");

    /** Accessibility id — MA-LOC-001 §11 primary locator; alternative resource-id {@code billingAddressCB}. {@code checked="true"} by default (Phase 12.3 runtime evidence); not interacted with by TC-022. */
    public static final By BILLING_ADDRESS_CHECKBOX = AppiumBy.accessibilityId("Select if User billing address and shipping address are same");

    /** Accessibility id — third reuse of resource-id {@code paymentBtn} (MA-LOC-001 §10/§11); never the bare resource-id. Not tapped by TC-022. */
    public static final By REVIEW_ORDER_BUTTON = AppiumBy.accessibilityId("Saves payment info and launches screen to review checkout data");

    private PaymentLocators() {
    }
}
