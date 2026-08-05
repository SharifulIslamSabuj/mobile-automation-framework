package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.ProductDetailsLocators;
import com.mobileautomation.framework.utils.ScrollUtility;

/**
 * Page Object for the Product Details screen (MA-LOC-001 §8).
 * <p>
 * <b>Purpose:</b> encapsulate a single product's detail view — information
 * display, color/quantity controls, and the Add to Cart action — behind
 * user-facing methods.
 * <p>
 * <b>Dependencies:</b> {@link BasePage} (inherited — {@code elementActions}
 * for interaction, {@code navigationHelper} for back navigation),
 * {@link ProductDetailsLocators}.
 * <p>
 * <b>Responsibilities:</b> read product name/price/image state, select a
 * color, adjust quantity, add to cart, navigate back.
 * <p>
 * <b>Limitations:</b> there is no dedicated "remove from cart" control on
 * this screen — MA-LOC-001 §8 documents only that reducing quantity to zero
 * disables the Add-to-Cart button; the actual Remove control ({@code removeBt})
 * exists solely on the Cart screen (MA-LOC-001 §9), which is explicitly out
 * of this phase's scope (no {@code CartPage}). {@link #decreaseQuantity()}
 * is the closest legitimate mapping available on this screen — documented
 * here rather than inventing a removal control that does not exist. Rating
 * stars and the color-selected indicator ({@code aroundIV}) are not exposed
 * — neither is needed by this phase's pilot scope, and the former requires
 * drawable-state assertion support the framework does not yet have. Back
 * navigation uses the system back gesture via {@code NavigationHelper}
 * (confirmed as the correct mechanism by TC-029) — there is no in-app back
 * button on this screen.
 */
public class ProductDetailsPage extends BasePage {

    /** @return the product name as bound on this screen (a runtime-set field, distinct from the catalog card's own name element — MA-LOC-001 §8). */
    public String getProductName() {
        return elementActions.getText(ProductDetailsLocators.PRODUCT_NAME);
    }

    /** @return the product price. */
    public String getProductPrice() {
        return elementActions.getText(ProductDetailsLocators.PRODUCT_PRICE);
    }

    /** @return whether the product image is displayed. */
    public boolean isProductImageDisplayed() {
        return elementActions.isDisplayed(ProductDetailsLocators.PRODUCT_IMAGE);
    }

    /** Taps the Add to Cart button. */
    public void addToCart() {
        elementActions.click(ProductDetailsLocators.ADD_TO_CART_BUTTON);
    }

    /** @return whether the Add To Cart button is displayed. */
    public boolean isAddToCartButtonDisplayed() {
        return elementActions.isDisplayed(ProductDetailsLocators.ADD_TO_CART_BUTTON);
    }

    /** @return whether the Add To Cart button is enabled (interactable). */
    public boolean isAddToCartButtonEnabled() {
        return elementActions.isEnabled(ProductDetailsLocators.ADD_TO_CART_BUTTON);
    }

    /** @return whether the Quantity Selector's three elements (decrease control, value, increase control) are all displayed. */
    public boolean isQuantitySelectorDisplayed() {
        return elementActions.isDisplayed(ProductDetailsLocators.DECREASE_QUANTITY_BUTTON)
                && elementActions.isDisplayed(ProductDetailsLocators.QUANTITY_VALUE)
                && elementActions.isDisplayed(ProductDetailsLocators.INCREASE_QUANTITY_BUTTON);
    }

    /** @return whether the Quantity Selector's decrease and increase controls are both enabled (interactable). */
    public boolean isQuantitySelectorInteractable() {
        return elementActions.isEnabled(ProductDetailsLocators.DECREASE_QUANTITY_BUTTON)
                && elementActions.isEnabled(ProductDetailsLocators.INCREASE_QUANTITY_BUTTON);
    }

    /** @return whether the Color Selector is displayed. */
    public boolean isColorSelectorDisplayed() {
        return elementActions.isDisplayed(ProductDetailsLocators.COLOR_LIST);
    }

    /**
     * @return the currently-rendered color swatch's {@code content-desc}
     * value (e.g. {@code "Unknown color"}) — read generically by resource-id
     * ({@link ProductDetailsLocators#COLOR_SWATCH_IMAGE}), not a specific
     * expected color name, since the actual value is exactly what TC-012
     * verifies (MA-LOC-001 §8's documented fallback: a color not matching
     * one of the AUT's four recognized constants renders as "Unknown color").
     */
    public String getSelectedColorDescription() {
        return elementActions.getAttribute(ProductDetailsLocators.COLOR_SWATCH_IMAGE, "content-desc");
    }

    /** Decreases the selected quantity by one — see class Javadoc Limitations for why this is also this screen's closest equivalent to "remove". */
    public void decreaseQuantity() {
        elementActions.click(ProductDetailsLocators.DECREASE_QUANTITY_BUTTON);
    }

    /** Increases the selected quantity by one. */
    public void increaseQuantity() {
        elementActions.click(ProductDetailsLocators.INCREASE_QUANTITY_BUTTON);
    }

    /** @return the currently displayed quantity value. */
    public String getQuantityValue() {
        return elementActions.getText(ProductDetailsLocators.QUANTITY_VALUE);
    }

    /**
     * Selects the color swatch matching {@code colorName} (e.g. {@code "Black"}).
     * Applies the AUT's own dynamic content-description pattern
     * ({@link ProductDetailsLocators#colorSwatch(String)}) — does not
     * invent a new locator strategy.
     */
    public void selectColor(String colorName) {
        elementActions.click(ProductDetailsLocators.colorSwatch(colorName));
    }

    /** Navigates back to the Product Catalog screen via the system back gesture. */
    public void navigateBack() {
        navigationHelper.back();
    }

    /**
     * Scrolls down until the Product Highlights label is visible — composes
     * {@code utils.ScrollUtility#scrollToResourceId(String)} (Phase 5,
     * already used by {@code ProductsPage#scrollToProduct(String)} for the
     * same reason: no new scrolling logic). Added Phase 10.3 for TC-013.
     */
    public void scrollToHighlights() {
        ScrollUtility.scrollToResourceId(ProductDetailsLocators.PRODUCT_HIGHLIGHTS_RESOURCE_ID);
    }

    /** Scrolls one page back up — used after {@link #scrollToHighlights()} to confirm the screen returns to its original, fully-available state. Added Phase 10.3 for TC-013. */
    public void scrollToTop() {
        ScrollUtility.scrollUp();
    }

    /** @return whether the Product Highlights label is currently displayed. Added Phase 10.3 for TC-013. */
    public boolean isProductHighlightsDisplayed() {
        return elementActions.isDisplayed(ProductDetailsLocators.PRODUCT_HIGHLIGHTS_LABEL);
    }

    /** @return whether this screen's product name element is displayed. */
    public boolean isDisplayed() {
        return elementActions.isDisplayed(ProductDetailsLocators.PRODUCT_NAME);
    }
}
