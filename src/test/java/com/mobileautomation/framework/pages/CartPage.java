package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.CartLocators;
import com.mobileautomation.framework.utils.WaitUtility;

/**
 * Page Object for the Cart screen (MA-LOC-001 §9).
 * <p>
 * Added Phase 9.5B, narrowly scoped to what TC-012's Business Verification
 * requires: confirming the added Pilot Product's Name and Price appear as a
 * line item. Extended Phase 11.2 with Total Items/Total Price reading
 * (TC-014/TC-017), Phase 11.3 with quantity increase/decrease/read
 * (TC-015), Phase 11.4 with item removal and empty-cart state reading
 * (TC-016), and Phase 11.8 with the Proceed To Checkout action (TC-018).
 * This Page Object's own responsibility ends at tapping that button — the
 * destination screen (Login or Shipping Address, per Phase 11.7's verified
 * runtime evidence) is handled by {@code LoginPage} and the new, minimally-scoped
 * {@code CheckoutPage} respectively; {@code CartTest} orchestrates the branch.
 * <p>
 * <b>{@link #removeItem()} note:</b> calls {@code WaitUtility.waitForInvisibility(By)}
 * directly rather than through {@code elementActions}, because
 * {@code ElementActions} does not currently expose an invisibility-wait
 * wrapper (only {@code isDisplayed(By)}, a positive-visibility check) —
 * this is the one method in this class that doesn't route through
 * {@code elementActions}. This is a deliberate, minimal use of an existing,
 * unmodified {@code WaitUtility} capability (Phase 11.4 instruction:
 * "handle it using existing WaitUtility mechanisms"), not a framework
 * change. It does forgo {@code ElementActions}' usual failure
 * logging/screenshot/exception-wrapping safety net for this one call — see
 * Phase 11.4's Issues Found for the resulting minor, non-blocking
 * observation.
 * <p>
 * <b>Single-item assumption:</b> {@link #getFirstItemName()} and
 * {@link #getFirstItemPrice()} each read index 0 of two independently
 * resolved lists. This is safe only because TC-012's pilot flow guarantees
 * exactly one item in the Cart at this point — both lists are guaranteed
 * length 1, so there is no index-correlation risk to begin with. This must
 * <b>not</b> be generalized to a multi-item cart without the same
 * parent-child scoping {@code ProductsPage} uses for the Catalog (see that
 * class's Javadoc for why independent-list index correlation is otherwise
 * unsound).
 */
public class CartPage extends BasePage {

    /** @return whether the Cart's item list is displayed. */
    public boolean isDisplayed() {
        return elementActions.isDisplayed(CartLocators.ITEM_LIST);
    }

    /** @return the first (and, for TC-012's single-item flow, only) cart item's product name. */
    public String getFirstItemName() {
        return elementActions.findAll(CartLocators.ITEM_NAME).get(0).getText();
    }

    /** @return the first (and, for TC-012's single-item flow, only) cart item's product price. */
    public String getFirstItemPrice() {
        return elementActions.findAll(CartLocators.ITEM_PRICE).get(0).getText();
    }

    /** @return whether the Total Items Count is displayed. Added Phase 11.2 for TC-014. */
    public boolean isTotalItemsDisplayed() {
        return elementActions.isDisplayed(CartLocators.TOTAL_ITEMS_COUNT);
    }

    /** @return whether the Total Price is displayed. Added Phase 11.2 for TC-014. */
    public boolean isTotalPriceDisplayed() {
        return elementActions.isDisplayed(CartLocators.TOTAL_PRICE);
    }

    /** @return the Total Items Count text (e.g. {@code "1 Items"} — MA-LOC-001 §9). Added Phase 11.2 for TC-017. */
    public String getTotalItems() {
        return elementActions.getText(CartLocators.TOTAL_ITEMS_COUNT);
    }

    /** @return the Total Price text (e.g. {@code "$ 29.99"} — MA-LOC-001 §9). Added Phase 11.2 for TC-017. */
    public String getTotalPrice() {
        return elementActions.getText(CartLocators.TOTAL_PRICE);
    }

    /**
     * @return the first (and, for the single-item cart flow this Page Object
     * assumes — see class Javadoc — only) cart item's displayed quantity.
     * Added Phase 11.3 for TC-015.
     */
    public String getQuantity() {
        return elementActions.getText(CartLocators.QUANTITY_VALUE);
    }

    /** Increases the (single) cart item's quantity by one. Added Phase 11.3 for TC-015. */
    public void increaseQuantity() {
        elementActions.click(CartLocators.INCREASE_QUANTITY_BUTTON);
    }

    /** Decreases the (single) cart item's quantity by one — at quantity 1 this removes the item (LOCATOR_REPOSITORY.md line 183), not exercised by TC-015, which only decreases from 2 back to 1. Added Phase 11.3 for TC-015. */
    public void decreaseQuantity() {
        elementActions.click(CartLocators.DECREASE_QUANTITY_BUTTON);
    }

    /** @return whether the Increase Quantity control is displayed. Added Phase 11.3 for TC-015. */
    public boolean isIncreaseButtonDisplayed() {
        return elementActions.isDisplayed(CartLocators.INCREASE_QUANTITY_BUTTON);
    }

    /** @return whether the Decrease Quantity control is displayed. Added Phase 11.3 for TC-015. */
    public boolean isDecreaseButtonDisplayed() {
        return elementActions.isDisplayed(CartLocators.DECREASE_QUANTITY_BUTTON);
    }

    /**
     * Taps the Remove Item control for the (single) cart item, then waits
     * for its row to genuinely disappear before returning — the Locator
     * Repository documents a source-confirmed 5-second simulated
     * UI-thread block on this tap (LOCATOR_REPOSITORY.md line 185,
     * "intentionally introduced" latency, confirmed in
     * {@code CartItemAdapter.java}). Waits on the real UI condition
     * (invisibility of the item's name element) rather than a fixed sleep
     * — see class Javadoc for why this is the one method here that calls
     * {@code WaitUtility} directly. Added Phase 11.4 for TC-016.
     */
    public void removeItem() {
        elementActions.click(CartLocators.REMOVE_BUTTON);
        WaitUtility.waitForInvisibility(CartLocators.ITEM_NAME);
    }

    /** @return whether a cart item's Product Name is currently displayed. Added Phase 11.4 for TC-016. */
    public boolean isItemPresent() {
        return elementActions.isDisplayed(CartLocators.ITEM_NAME);
    }

    /**
     * @return whether the Cart is in its empty state — read via the
     * Empty-Cart Illustration Container, the Locator Repository's own
     * documented state-detection element for cart empty vs. populated
     * (LOCATOR_REPOSITORY.md line 175), not an invented heuristic.
     * Added Phase 11.4 for TC-016.
     */
    public boolean isCartEmpty() {
        return elementActions.isDisplayed(CartLocators.EMPTY_CART_ILLUSTRATION_CONTAINER);
    }

    /** @return the Empty-Cart Title text (e.g. {@code "No Items"}). Added Phase 11.4 for TC-016. */
    public String getEmptyCartMessage() {
        return elementActions.getText(CartLocators.EMPTY_CART_TITLE);
    }

    /** @return whether the Proceed To Checkout button is displayed. Added Phase 11.8 for TC-018. */
    public boolean isProceedToCheckoutButtonDisplayed() {
        return elementActions.isDisplayed(CartLocators.PROCEED_TO_CHECKOUT_BUTTON);
    }

    /** @return whether the Proceed To Checkout button is enabled (interactable). Added Phase 11.8 for TC-018. */
    public boolean isProceedToCheckoutButtonEnabled() {
        return elementActions.isEnabled(CartLocators.PROCEED_TO_CHECKOUT_BUTTON);
    }

    /**
     * Taps the Proceed To Checkout button. Does not itself determine the
     * resulting screen — per Phase 11.7's verified runtime evidence, the
     * destination is conditional on authentication state (Login screen if
     * anonymous, Shipping Address screen if already authenticated); the
     * caller (test orchestration) branches on that. Added Phase 11.8 for TC-018.
     */
    public void tapProceedToCheckout() {
        elementActions.click(CartLocators.PROCEED_TO_CHECKOUT_BUTTON);
    }
}
