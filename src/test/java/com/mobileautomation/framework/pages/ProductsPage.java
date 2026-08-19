package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.exceptions.ElementActionException;
import com.mobileautomation.framework.locators.NavigationDrawerLocators;
import com.mobileautomation.framework.locators.ProductsLocators;
import com.mobileautomation.framework.utils.ScrollUtility;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Optional;

/**
 * Page Object for the Product Catalog screen (MA-LOC-001 §6).
 * <p>
 * <b>Purpose:</b> encapsulate the product list, its shared header controls,
 * and the cart badge behind user-facing actions.
 * <p>
 * <b>Dependencies:</b> {@link BasePage} (inherited), {@link ProductsLocators},
 * {@code core.ElementActions#findAll(By)} (added this phase specifically to
 * support this screen's repeating RecyclerView fields, MA-LOC-001 §19.1).
 * <p>
 * <b>Responsibilities:</b> read/select products by index, locate and verify
 * a product card by name for TC-012's pilot flow, open the Sort dialog
 * (button tap only), open the Navigation Drawer and select a drawer item by
 * text, tap the cart icon, read the cart badge.
 * <p>
 * <b>Limitations:</b> the shared header (Menu/Sort/Cart/Logo) does not
 * cleanly fit any of the ten existing generic components — each has an
 * independent action, and every existing header-shaped component models
 * exactly one action alongside a title (see MA-PAD-001 §6, an already-approved
 * decision this phase does not revisit) — so header controls are accessed
 * directly rather than through a forced component composition. The Sort
 * dialog's four options (Name/Price Ascending/Descending) are modeled
 * directly on this Page Object rather than as a separate dialog Page/Component
 * — added Phase 14.1 (TC-006) for the same reason the header controls are:
 * the dialog is part of this screen's own UI, triggered by {@link #tapSort()}.
 * {@link #verifyProductCardExists(String)} /
 * {@link #getCardPrice(String)} / {@link #openProductCard(String)} are the
 * correct navigation methods as of Phase 9.5C/D — each resolves the card's
 * image/price via a single whole-document XPath anchored on the product's
 * exact name ({@link ProductsLocators#productImageForCard(String)}/
 * {@link ProductsLocators#productPriceForCard(String)}), evaluated against
 * the driver directly. This retires three prior approaches in sequence,
 * each superseded by live evidence the previous one was unsound: Phase
 * 9.2's index correlation and Phase 9.5.1's geometric bounds-matching both
 * resolved two independent {@code findAll} lists, which Phase 9.5.1 proved
 * can differ in length; Phase 9.5B's parent-child scoped traversal (via
 * {@code ElementActions#findWithin}) avoided that, but a live diagnostic in
 * Phase 9.5C proved Appium's UiAutomator2 driver does not support upward
 * (parent/sibling-axis) traversal in a scoped/nested find at all — only a
 * whole-document query works for a sibling relationship like this one.
 * {@link #selectProduct(int)}/{@link #getProductName(int)}/
 * {@link #getProductPrice(int)} each resolve only one locator's list, so
 * they don't have that cross-list risk, but still carry the single-list
 * staleness risk documented on {@link #selectProduct(int)}. Rating stars
 * are not modeled — MA-LOC-001 §20.2 flags them as unreliable
 * accessibility-id targets requiring drawable-state assertions the
 * framework does not yet support.
 */
public class ProductsPage extends BasePage {

    /** @return whether the Catalog screen's title is displayed. */
    public boolean isDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.SCREEN_TITLE);
    }

    /** @return the number of product cards currently resolvable on screen. */
    public int getProductCount() {
        return elementActions.findAll(ProductsLocators.PRODUCT_NAME).size();
    }

    /** @return the product name at {@code index} (0-based, in on-screen order). */
    public String getProductName(int index) {
        return productElementAt(ProductsLocators.PRODUCT_NAME, index).getText();
    }

    /** @return the product price at {@code index} (0-based, in on-screen order). */
    public String getProductPrice(int index) {
        return productElementAt(ProductsLocators.PRODUCT_PRICE, index).getText();
    }

    /**
     * Taps the product image at {@code index} (0-based) to navigate to its
     * Product Details screen. Taps the image, not the name text — confirmed
     * live (Phase 9.5, real device UI-hierarchy dump) that the name text
     * ({@code titleTV}) is not clickable and has no clickable ancestor;
     * only the product image ({@code productIV}) is.
     * <p>
     * <b>Caution:</b> {@code index} must come from a {@code findAll} snapshot
     * taken immediately before this call, on the same rendered state — the
     * RecyclerView's rendered set can shift between two independent
     * {@code findAll} calls (e.g. across a scroll), making an index resolved
     * earlier point at a different element by the time this method
     * re-resolves the list. Prefer {@link #locateProductCard(String)} +
     * {@link #openProductCard(WebElement)} when the target was located by
     * name rather than a freshly-taken index.
     */
    public void selectProduct(int index) {
        productElementAt(ProductsLocators.PRODUCT_IMAGE, index).click();
    }

    /**
     * Locates the Product Card whose displayed name exactly matches
     * {@code productName}, scrolling it into view first if needed (reuses
     * {@link #scrollToProduct(String)} — no new scrolling logic), and
     * returns that displayed name text — the caller asserts it against the
     * expected value for an explicit, reported Product Card Name
     * verification (MA-TC-001 TC-012 Step 3), rather than treating a
     * successful lookup alone as the verification.
     *
     * @throws ElementActionException if no product with that exact name is found
     */
    public String verifyProductCardExists(String productName) {
        scrollToProduct(productName);
        List<WebElement> names = elementActions.findAll(ProductsLocators.PRODUCT_NAME);
        for (WebElement name : names) {
            String text = name.getText();
            if (text.equals(productName)) {
                return text;
            }
        }
        throw new ElementActionException(
                "No product named '" + productName + "' found in the currently-rendered catalog.");
    }

    /**
     * @return the Product Price displayed on the card whose Product Title
     * text is exactly {@code productName} — resolved via
     * {@link ProductsLocators#productPriceForCard(String)}, a single
     * whole-document XPath anchored on that exact text (Phase 9.5C/D fix).
     * Replaces the Phase 9.5B parent-child-traversal approach, which a live
     * diagnostic proved unsound: Appium's UiAutomator2 driver does not
     * support upward (parent/sibling-axis) traversal in a scoped/nested
     * find — only the whole-document form works.
     * <p>
     * Scrolls one page forward before reading the price only if needed —
     * Phase 17.6E found {@link #verifyProductCardExists(String)}'s own
     * scroll only guarantees the name text meets
     * {@code UiScrollable.scrollIntoView}'s minimal visibility criterion,
     * not that the price line beneath it in the same card is visible; Phase
     * 17.6G found re-calling that same criteria-based scroll (the original
     * fix attempt) is consequently a no-op. Phase 17.6H's fix
     * ({@link ScrollUtility#scrollDown()}, called unconditionally) traded
     * that for the opposite failure: an unconditional forward scroll can
     * move the viewport past a price line that {@code scrollIntoView}
     * already brought into view, taking the whole card off-screen (Phase
     * AF real-device evidence, confirmed via captured page source showing
     * a different, unrelated pair of cards on screen at query time). This
     * implementation only performs that forward scroll when the price for
     * this specific card is not already visible, so the already-satisfied
     * common case is never disturbed. There is no unique locator to scroll
     * to directly for a specific card's price (its content-desc is shared
     * by every card), so the check re-uses the same name-anchored XPath
     * {@link #getCardPrice(String)} ultimately reads from.
     */
    public String getCardPrice(String productName) {
        scrollToProduct(productName);
        By priceLocator = ProductsLocators.productPriceForCard(productName);
        if (!elementActions.isDisplayed(priceLocator)) {
            ScrollUtility.scrollDown();
        }
        return elementActions.getText(priceLocator);
    }

    /**
     * Taps the Product Image belonging to the card whose Product Title text
     * is exactly {@code productName}, navigating to its Product Details
     * screen — resolved via {@link ProductsLocators#productImageForCard(String)},
     * the same whole-document approach as {@link #getCardPrice(String)}.
     * Retires, in order: Phase 9.2's index correlation, Phase 9.5.1's
     * geometric bounds-matching, and Phase 9.5B's parent-child scoped
     * traversal — each superseded by live evidence the prior approach was
     * unsound (see class Javadoc and {@link ProductsLocators#productImageForCard(String)}).
     */
    public void openProductCard(String productName) {
        elementActions.click(ProductsLocators.productImageForCard(productName));
    }

    /**
     * Scrolls the product list until a product with the exact given name is
     * visible. Added Phase 9.5: the catalog has more products than fit in
     * one viewport (confirmed live — only the "Sauce Labs Backpack" variants
     * render initially), and {@link #findProductIndexByName} /
     * {@link #getProductCount} only see currently-rendered RecyclerView
     * items (MA-LOC-001 §19.1). Composes {@code utils.ScrollUtility}
     * (Phase 5, previously unused by any Page Object) rather than
     * duplicating scroll logic.
     */
    public void scrollToProduct(String productName) {
        ScrollUtility.scrollToText(productName);
    }

    /** @return the 0-based index of the first product whose name exactly matches {@code productName}, or {@link Optional#empty()} if none does. */
    public Optional<Integer> findProductIndexByName(String productName) {
        List<WebElement> names = elementActions.findAll(ProductsLocators.PRODUCT_NAME);
        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).getText().equals(productName)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    /** Opens the Sort dialog. */
    public void tapSort() {
        elementActions.click(ProductsLocators.SORT_BUTTON);
    }

    /** @return whether the Sort dialog's title ("Sort by:") is displayed. */
    public boolean isSortDialogTitleDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.SORT_DIALOG_TITLE);
    }

    /** @return whether the Name - Ascending sort option is displayed in the Sort dialog. */
    public boolean isSortNameAscendingOptionDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.SORT_NAME_ASCENDING_OPTION);
    }

    /** @return whether Name - Ascending is the catalog's currently active sort (its checkmark is visible — MA-LOC-001 §7). */
    public boolean isSortNameAscendingSelected() {
        return elementActions.isDisplayed(ProductsLocators.SORT_NAME_ASCENDING_CHECKMARK);
    }

    /** Taps the Name - Ascending sort option — dismisses the Sort dialog automatically and re-orders the catalog (MA-LOC-001 §7, confirmed Phase 14.1). */
    public void tapSortNameAscending() {
        elementActions.click(ProductsLocators.SORT_NAME_ASCENDING_OPTION);
    }

    /** @return whether the Name - Descending sort option is displayed in the Sort dialog. */
    public boolean isSortNameDescendingOptionDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.SORT_NAME_DESCENDING_OPTION);
    }

    /** @return whether Name - Descending is the catalog's currently active sort (its checkmark is visible). */
    public boolean isSortNameDescendingSelected() {
        return elementActions.isDisplayed(ProductsLocators.SORT_NAME_DESCENDING_CHECKMARK);
    }

    /** @see #tapSortNameAscending() */
    public void tapSortNameDescending() {
        elementActions.click(ProductsLocators.SORT_NAME_DESCENDING_OPTION);
    }

    /** @return whether the Price - Ascending sort option is displayed in the Sort dialog. */
    public boolean isSortPriceAscendingOptionDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.SORT_PRICE_ASCENDING_OPTION);
    }

    /** @return whether Price - Ascending is the catalog's currently active sort (its checkmark is visible). */
    public boolean isSortPriceAscendingSelected() {
        return elementActions.isDisplayed(ProductsLocators.SORT_PRICE_ASCENDING_CHECKMARK);
    }

    /** @see #tapSortNameAscending() */
    public void tapSortPriceAscending() {
        elementActions.click(ProductsLocators.SORT_PRICE_ASCENDING_OPTION);
    }

    /** @return whether the Price - Descending sort option is displayed in the Sort dialog. */
    public boolean isSortPriceDescendingOptionDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.SORT_PRICE_DESCENDING_OPTION);
    }

    /** @return whether Price - Descending is the catalog's currently active sort (its checkmark is visible). */
    public boolean isSortPriceDescendingSelected() {
        return elementActions.isDisplayed(ProductsLocators.SORT_PRICE_DESCENDING_CHECKMARK);
    }

    /** @see #tapSortNameAscending() */
    public void tapSortPriceDescending() {
        elementActions.click(ProductsLocators.SORT_PRICE_DESCENDING_OPTION);
    }

    /** Opens the Navigation Drawer. */
    public void tapMenu() {
        elementActions.click(ProductsLocators.MENU_BUTTON);
    }

    /**
     * Taps a Navigation Drawer item by its exact visible text (e.g. {@code "Login"}).
     * Added Phase 9.4: reaching the Login screen requires this — MA-TC-001's
     * own precondition chain (TC-004 ← TC-003 ← TC-002 ← TC-008) opens the
     * Drawer from this screen and selects an item by row, and no dedicated
     * Navigation Drawer Page Object is in scope. Kept generic (any item
     * text), not a {@code tapLoginDrawerItem()} single-purpose method.
     */
    public void tapDrawerItem(String itemText) {
        elementActions.click(NavigationDrawerLocators.drawerItem(itemText));
    }

    /**
     * @return whether a Navigation Drawer item with the exact given text is
     * currently displayed — a read-only state check (e.g. confirming "Log Out"
     * vs "Log In" reflects the current authentication state), distinct from
     * {@link #tapDrawerItem(String)} which taps. Added Phase 9.5G for TC-004's
     * business evidence enhancement.
     */
    public boolean isDrawerItemDisplayed(String itemText) {
        return elementActions.isDisplayed(NavigationDrawerLocators.drawerItem(itemText));
    }

    /** Taps the Cart icon. */
    public void tapCart() {
        elementActions.click(ProductsLocators.CART_BUTTON);
    }

    /** @return whether the cart badge is currently rendered — it is conditionally present, not merely hidden, when the cart is empty (MA-LOC-001 §6). */
    public boolean isCartBadgeDisplayed() {
        return elementActions.isDisplayed(ProductsLocators.CART_BADGE_CONTAINER);
    }

    /** @return the cart badge's count text, or {@link Optional#empty()} if the badge is not currently rendered — never reads the count without first checking presence (MA-LOC-001 §6's explicit warning). */
    public Optional<String> getCartBadgeCount() {
        return isCartBadgeDisplayed()
                ? Optional.of(elementActions.getText(ProductsLocators.CART_BADGE_COUNT))
                : Optional.empty();
    }

    private WebElement productElementAt(By locator, int index) {
        List<WebElement> elements = elementActions.findAll(locator);
        if (index < 0 || index >= elements.size()) {
            throw new ElementActionException("Product index " + index
                    + " out of bounds — catalog currently has " + elements.size() + " product(s).");
        }
        return elements.get(index);
    }
}
