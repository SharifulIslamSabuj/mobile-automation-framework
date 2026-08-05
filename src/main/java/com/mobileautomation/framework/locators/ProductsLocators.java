package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Product Catalog screen locators. Every value is quoted directly from
 * MA-LOC-001 §6 (Locator Repository) — none is invented, none is duplicated
 * elsewhere. Product Image/Name/Price/Rating repeat identically per
 * RecyclerView item (MA-LOC-001 §19.1) — these locators resolve to every
 * matching element on screen; callers must use {@code core.ElementActions.findAll(By)}
 * and index into the result, never assume a single match.
 */
public final class ProductsLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By SCREEN_TITLE = AppiumBy.accessibilityId("title");
    public static final By PRODUCT_LIST = AppiumBy.accessibilityId("Displays all products of catalog");
    public static final By PRODUCT_NAME = AppiumBy.accessibilityId("Product Title");
    public static final By PRODUCT_PRICE = AppiumBy.accessibilityId("Product Price");
    public static final By PRODUCT_IMAGE = AppiumBy.accessibilityId("Product Image");
    public static final By SORT_BUTTON = AppiumBy.accessibilityId("Shows current sorting order and displays available sorting options");
    public static final By MENU_BUTTON = AppiumBy.accessibilityId("View menu");
    public static final By CART_BUTTON = AppiumBy.accessibilityId("View cart");
    public static final By CART_BADGE_CONTAINER = By.id(PACKAGE + "cartCircleRL");
    public static final By CART_BADGE_COUNT = By.id(PACKAGE + "cartTV");

    /**
     * Sort Dialog (MA-LOC-001 §7, source: {@code sort_dialog.xml}), triggered
     * by {@link #SORT_BUTTON}. Added Phase 14.1 (TC-006). Every value is
     * quoted directly from MA-LOC-001 §7's source-decompiled evidence, then
     * corroborated on the real device this phase: tapping any of the four
     * options auto-dismisses the dialog and re-orders the catalog, and the
     * corresponding checkmark's visibility tracks the currently active sort
     * — confirmed for all four options, not just Price Ascending (the only
     * option MA-LOC-001 §7 had previously verified end-to-end). Real-device
     * evidence also showed Name Ascending is the Product Catalog's default
     * sort state (its checkmark is visible without any interaction) —
     * consistent with MA-LOC-001 §7 already documenting the other three
     * checkmarks, but not this one, as {@code invisible} by default.
     */
    public static final By SORT_DIALOG_TITLE = By.id(PACKAGE + "sortTV");
    public static final By SORT_NAME_ASCENDING_OPTION = AppiumBy.accessibilityId("Ascending order by name");
    public static final By SORT_NAME_ASCENDING_CHECKMARK = By.id(PACKAGE + "tickNameAscIV");
    public static final By SORT_NAME_DESCENDING_OPTION = AppiumBy.accessibilityId("Descending order by name");
    public static final By SORT_NAME_DESCENDING_CHECKMARK = By.id(PACKAGE + "tickNameDesIV");
    public static final By SORT_PRICE_ASCENDING_OPTION = AppiumBy.accessibilityId("Ascending order by price");
    public static final By SORT_PRICE_ASCENDING_CHECKMARK = By.id(PACKAGE + "tickPriceAscIV");
    public static final By SORT_PRICE_DESCENDING_OPTION = AppiumBy.accessibilityId("Descending order by price");
    public static final By SORT_PRICE_DESCENDING_CHECKMARK = By.id(PACKAGE + "tickPriceDscIV");

    /**
     * Product Image / Price belonging to the Product Card whose Product
     * Title text is exactly {@code productName} — a single whole-document
     * XPath anchored on that exact text, evaluated against the driver
     * directly (via a normal {@code ElementActions.click(By)}/{@code getText(By)}
     * call), never via a scoped/nested {@code WebElement#findElement} call.
     * <p>
     * Added Phase 9.5C, replacing the Phase 9.5B {@code PRODUCT_IMAGE_RELATIVE_TO_CARD}/
     * {@code PRODUCT_PRICE_RELATIVE_TO_CARD} constants (removed): a live
     * diagnostic against the real device (this session) proved Appium's
     * UiAutomator2 driver does not support upward (parent- or sibling-axis)
     * traversal in a scoped/nested find — {@code element.findElement(By.xpath(".."))}
     * and every sibling-axis variant tried failed identically with
     * {@code NoSuchElementException}, while the identical structural
     * relationship expressed as a single whole-document query (anchored on
     * the matched card's exact text) and resolved against the driver
     * directly succeeded every time. {@code WaitUtility}/{@code ElementActions}'
     * scoped-find capability (added Phase 9.5B) itself remains correct and
     * reusable for genuine <em>descendant</em> searches — see their own
     * Javadoc — it was simply the wrong tool for this specific sibling
     * relationship.
     */
    public static By productImageForCard(String productName) {
        return By.xpath("//*[@text=" + xpathLiteral(productName) + "]/parent::*/*[@content-desc='Product Image']");
    }

    /** @see #productImageForCard(String) */
    public static By productPriceForCard(String productName) {
        return By.xpath("//*[@text=" + xpathLiteral(productName) + "]/parent::*/*[@content-desc='Product Price']");
    }

    /**
     * Quotes {@code value} for safe embedding as an XPath 1.0 string
     * literal — single-quoted normally, double-quoted if it contains a
     * single quote, or built via {@code concat()} if it contains both quote
     * characters (XPath 1.0 has no in-literal escape sequence).
     */
    private static String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        StringBuilder concat = new StringBuilder("concat(");
        String[] parts = value.split("'", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                concat.append(", \"'\", ");
            }
            concat.append("'").append(parts[i]).append("'");
        }
        return concat.append(")").toString();
    }

    private ProductsLocators() {
    }
}
