package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Cart screen locators. Every value is quoted directly from MA-LOC-001 §9
 * (Locator Repository) — none is invented. Added Phase 9.5B, narrowly scoped
 * to what TC-012's Business Verification requires (confirming the added
 * product's Name and Price appear as a line item) — not a full Cart locator
 * set. Item Name/Price reuse the same resource-ids used on the Catalog
 * screen (MA-LOC-001 §20.1, confirmed cross-screen reuse); callers must
 * resolve these against the Cart's own screen context, never assume a
 * single global match.
 */
public final class CartLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By ITEM_LIST = AppiumBy.accessibilityId("Displays list of selected products");
    public static final By ITEM_NAME = By.id(PACKAGE + "titleTV");
    public static final By ITEM_PRICE = By.id(PACKAGE + "priceTV");

    /** Total item count, e.g. {@code "1 Items"} (MA-LOC-001 §9 / LOCATOR_REPOSITORY.md line 186). Added Phase 11.2 for TC-014/TC-017. */
    public static final By TOTAL_ITEMS_COUNT = By.id(PACKAGE + "itemsTV");
    /** Total price, e.g. {@code "$ 29.99"} (MA-LOC-001 §9 / LOCATOR_REPOSITORY.md line 187). Added Phase 11.2 for TC-014/TC-017. */
    public static final By TOTAL_PRICE = By.id(PACKAGE + "totalPriceTV");

    /** Cart item quantity value (resource-id `noTV`, repeats per row — LOCATOR_REPOSITORY.md line 182). Same resource-id as Product Details' quantity value, deliberately not shared — screen-scoped, per this class's own convention (see class Javadoc). Added Phase 11.3 for TC-015. */
    public static final By QUANTITY_VALUE = By.id(PACKAGE + "noTV");
    /** Cart item Decrease Quantity control (LOCATOR_REPOSITORY.md line 183 — at quantity 1, tapping this removes the item per source evidence, not exercised by TC-015). Added Phase 11.3 for TC-015. */
    public static final By DECREASE_QUANTITY_BUTTON = AppiumBy.accessibilityId("Decrease item quantity");
    /** Cart item Increase Quantity control (LOCATOR_REPOSITORY.md line 184). Added Phase 11.3 for TC-015. */
    public static final By INCREASE_QUANTITY_BUTTON = AppiumBy.accessibilityId("Increase item quantity");

    /** Remove Item control, repeats per row (safe for this Page Object's single-item cart assumption — see {@code CartPage} class Javadoc). LOCATOR_REPOSITORY.md line 185: source-confirmed 5-second simulated UI-thread block on tap. Added Phase 11.4 for TC-016. */
    public static final By REMOVE_BUTTON = AppiumBy.accessibilityId("Removes product from cart");
    /** Empty-Cart Illustration Container — the Locator Repository's own documented state-detection element for cart empty vs. populated (LOCATOR_REPOSITORY.md line 175: {@code visibility="gone"} when cart has items). Added Phase 11.4 for TC-016. */
    public static final By EMPTY_CART_ILLUSTRATION_CONTAINER = By.id(PACKAGE + "noItemCL");
    /** Empty-Cart Title text, e.g. {@code "No Items"} (LOCATOR_REPOSITORY.md line 174). Added Phase 11.4 for TC-016. */
    public static final By EMPTY_CART_TITLE = By.id(PACKAGE + "noItemTitleTV");

    /** Proceed To Checkout button — accessibility id, never the reused resource-id {@code cartBt} (LOCATOR_REPOSITORY.md line 188: {@code cartBt} is reused for Product Details' "Add to Cart" — accessibility id is the reliable differentiator). Added Phase 11.8 for TC-018. */
    public static final By PROCEED_TO_CHECKOUT_BUTTON = AppiumBy.accessibilityId("Confirms products for checkout");

    private CartLocators() {
    }
}
