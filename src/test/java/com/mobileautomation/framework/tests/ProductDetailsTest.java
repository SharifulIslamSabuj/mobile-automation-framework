package com.mobileautomation.framework.tests;

import com.mobileautomation.framework.assertions.CommonAssertions;
import com.mobileautomation.framework.core.BaseTest;
import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.models.ProductItem;
import com.mobileautomation.framework.pages.CartPage;
import com.mobileautomation.framework.pages.ProductDetailsPage;
import com.mobileautomation.framework.pages.ProductsPage;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import org.testng.annotations.Test;

/**
 * TC-012 — Add to Cart Action (MA-TC-001 v1.3; MA-TD-001 TS-012 v1.2).
 * <p>
 * Rewritten Phase 9.5B around the approved Pilot Product — Sauce Labs
 * Backpack (violet), $29.99 (MA-TDD-001 §8.4, TD-004; supersedes the earlier
 * Sauce Labs Onesie dataset) — loaded exclusively from
 * {@code ProductDataFactory.pilotProduct()}, never hardcoded here.
 * <p>
 * Follows MA-TC-001 TC-012's 14 verified steps and this phase's mandated
 * assertion order: Product Card verified &rarr; Product Details displayed
 * &rarr; Product Name verified &rarr; Product Price verified &rarr; Controls
 * verified &rarr; Add To Cart executed &rarr; Cart Badge verified &rarr;
 * Cart contents verified. Each {@code CommonAssertions} call throws on
 * failure, so ordinary Java control flow already guarantees no downstream
 * assertion executes after an upstream one fails — no extra guard logic is
 * needed for that requirement.
 * <p>
 * Product Card location and navigation use {@code ProductsPage}'s
 * whole-document, name-anchored locators (Phase 9.5C/D) — not index
 * correlation, not geometric bounds-matching, and not a parent-child scoped
 * traversal. All three prior approaches were tried and retired in sequence
 * across Phase 9.2, 9.5.1, and 9.5B/C, each superseded by live evidence the
 * previous one was unsound — see {@code ProductsPage}'s class Javadoc for
 * the full history.
 * <p>
 * Phase 10.3 added {@link #selectProductColor()} (TC-010),
 * {@link #adjustProductQuantity()} (TC-011), and
 * {@link #scrollToProductHighlights()} (TC-013) as independent {@code @Test}
 * methods — each reported as its own pass/fail node, since MA-TC-001 tracks
 * them as separate Test Cases. Each reaches Product Details via
 * {@link #navigateToProductDetails(ProductItem, String)}, which reuses the
 * exact same {@code ProductsPage}/{@code ProductDetailsPage} navigation
 * methods {@link #addProductToCart()} (TC-012) already uses — TC-012 itself
 * is untouched. Reaching Product Details independently per test (rather than
 * chaining off TC-012) is deliberate: {@code ExecutionStrategy.ISOLATED}
 * resets the AUT before every test method, so no test can rely on another
 * test's leftover state, and MA-TC-001's own Phase 10.3 authorization
 * requires each Test Case runnable and reportable individually.
 */
public class ProductDetailsTest extends BaseTest {

    @Test(description = "TC-012 — Add to Cart Action")
    public void addProductToCart() {
        // Step 1 — Load the Pilot Product. Never hardcoded (MA-TDD-001 TD-004).
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product: name='{}', price={}", pilotProduct.name(), pilotProduct.price());

        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen");
        ScreenshotManager.captureScreenshot("tc012_01_catalog_before_selection");

        // Steps 2-4 — Locate and verify the Product Card. (Assertion Order #1: Product Card verified.)
        logger.info("Searching Product Catalog for Product Card '{}'.", pilotProduct.name());
        String cardName = productsPage.verifyProductCardExists(pilotProduct.name());
        String cardPrice = productsPage.getCardPrice(pilotProduct.name());
        logger.info("Product Card located; verifying Name and Price before navigation.");
        CommonAssertions.verifyText(cardName, pilotProduct.name(), "Product Card — Product Name");
        CommonAssertions.verifyText(cardPrice, formattedPrice(pilotProduct), "Product Card — Product Price");
        ScreenshotManager.captureScreenshot("tc012_01b_product_card_verified");

        // Step 5 — Tap the Product Image belonging to the verified Product Card.
        logger.info("Product Card verified; navigating to Product Details.");
        productsPage.openProductCard(pilotProduct.name());

        // Step 6 — Verify Product Details screen displayed. (Assertion Order #2.)
        ProductDetailsPage productDetailsPage = new ProductDetailsPage();
        CommonAssertions.verifyVisible(productDetailsPage.isProductImageDisplayed(), "Product Details — Product Image");
        CommonAssertions.verifyVisible(productDetailsPage.isDisplayed(), "Product Details screen");
        ScreenshotManager.captureScreenshot("tc012_02_product_details_after_navigation");

        // Steps 7-8 — Verify Product Name and Price on Product Details. (Assertion Order #3-4.)
        logger.info("Verifying Product Details identity against the loaded Pilot Product.");
        CommonAssertions.verifyText(productDetailsPage.getProductName(), pilotProduct.name(), "Product Details — Product Name");
        CommonAssertions.verifyText(productDetailsPage.getProductPrice(), formattedPrice(pilotProduct), "Product Details — Product Price");

        // Steps 9-11 — Verify controls. (Assertion Order #5.)
        logger.info("Verifying Product Details controls: Add To Cart, Quantity Selector, Color Selector.");
        CommonAssertions.verifyVisible(productDetailsPage.isAddToCartButtonDisplayed(), "Add To Cart button");
        CommonAssertions.verifyEnabled(productDetailsPage.isAddToCartButtonEnabled(), "Add To Cart button");
        CommonAssertions.verifyVisible(productDetailsPage.isQuantitySelectorDisplayed(), "Quantity Selector");
        CommonAssertions.verifyEnabled(productDetailsPage.isQuantitySelectorInteractable(), "Quantity Selector (decrease/increase controls)");
        CommonAssertions.verifyVisible(productDetailsPage.isColorSelectorDisplayed(), "Color Selector");
        // Verified documented behavior, not an assumption: "Violet" is not one of the AUT's four
        // recognized color constants (Black/Green/Gray/Blue — MA-LOC-001 §8), so the swatch
        // correctly falls through to "Unknown color". MA-TC-001 TC-012 Step 11 documents this exactly.
        CommonAssertions.verifyText(productDetailsPage.getSelectedColorDescription(), "Unknown color", "Color Selector content-description");

        // Step 12 — Tap Add To Cart. (Assertion Order #6.)
        logger.info("Executing business action: Add To Cart.");
        productDetailsPage.addToCart();

        // Step 13 — Verify Cart Badge. (Assertion Order #7.)
        CommonAssertions.verifyVisible(productsPage.isCartBadgeDisplayed(), "Cart Badge");
        CommonAssertions.verifyText(productsPage.getCartBadgeCount().orElse(null), "1", "Cart Badge value");

        // Step 14 — Open Cart and verify contents. (Assertion Order #8.)
        logger.info("Opening Cart to verify the added Pilot Product.");
        productsPage.tapCart();
        CartPage cartPage = new CartPage();
        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyText(cartPage.getFirstItemName(), pilotProduct.name(), "Cart line item — Product Name");
        CommonAssertions.verifyText(cartPage.getFirstItemPrice(), formattedPrice(pilotProduct), "Cart line item — Product Price");
        ScreenshotManager.captureScreenshot("tc012_03_cart_after_add_to_cart");

        logger.info("TC-012 completed: Pilot Product '{}' successfully added to Cart.", pilotProduct.name());
    }

    @Test(description = "TC-010 — Product Color Selection")
    public void selectProductColor() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-010: name='{}'.", pilotProduct.name());

        ProductDetailsPage productDetailsPage = navigateToProductDetails(pilotProduct, "tc010");

        CommonAssertions.verifyVisible(productDetailsPage.isColorSelectorDisplayed(), "Color Selector");
        String colorBeforeSelection = productDetailsPage.getSelectedColorDescription();
        logger.info("Color Selector's rendered swatch before interaction: '{}'.", colorBeforeSelection);

        // Applies the AUT's own dynamic content-desc pattern ("<ColorName> color") in reverse —
        // the swatch's own content-desc already IS "<colorName> color" (MA-LOC-001 §19.4), so the
        // color name to tap is derived from what is actually rendered, never hardcoded.
        String colorName = colorBeforeSelection.endsWith(" color")
                ? colorBeforeSelection.substring(0, colorBeforeSelection.length() - " color".length())
                : colorBeforeSelection;
        productDetailsPage.selectColor(colorName);
        ScreenshotManager.captureScreenshot("tc010_02_color_selector_after_interaction");

        CommonAssertions.verifyVisible(productDetailsPage.isColorSelectorDisplayed(), "Color Selector (post-interaction)");
        String colorAfterSelection = productDetailsPage.getSelectedColorDescription();
        logger.info("Color Selector's rendered swatch after interaction: '{}'.", colorAfterSelection);

        // Phase 10.2 evidence (real-device screenshot review) already established that the Pilot
        // Product renders exactly one color swatch — there is no alternative color to switch to.
        // Documenting that actual observed behavior (the description is unchanged and still reflects
        // a real, non-empty selected state) rather than forcing an assertion that a change occurred.
        CommonAssertions.verifyText(colorAfterSelection, colorBeforeSelection,
                "Color Selector content-description after interaction — single rendered swatch for this Pilot Product, no alternative color option exists to switch to (Phase 10.3 runtime evidence)");

        logger.info("TC-010 completed: Color Selector displayed and interactable for Pilot Product '{}'; "
                        + "swatch content-description before/after interaction: '{}'.",
                pilotProduct.name(), colorAfterSelection);
    }

    @Test(description = "TC-011 — Product Quantity Selection")
    public void adjustProductQuantity() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-011: name='{}'.", pilotProduct.name());

        ProductDetailsPage productDetailsPage = navigateToProductDetails(pilotProduct, "tc011");

        CommonAssertions.verifyVisible(productDetailsPage.isQuantitySelectorDisplayed(), "Quantity Selector");
        CommonAssertions.verifyEnabled(productDetailsPage.isQuantitySelectorInteractable(), "Quantity Selector (decrease/increase controls)");

        int initialQuantity = Integer.parseInt(productDetailsPage.getQuantityValue());
        logger.info("Quantity Selector initial value: {}.", initialQuantity);

        productDetailsPage.increaseQuantity();
        int increasedQuantity = Integer.parseInt(productDetailsPage.getQuantityValue());
        logger.info("Quantity Selector value after one Increase tap: {}.", increasedQuantity);
        CommonAssertions.verifyText(String.valueOf(increasedQuantity), String.valueOf(initialQuantity + 1),
                "Quantity value after one Increase tap");
        ScreenshotManager.captureScreenshot("tc011_02_quantity_after_increase");

        productDetailsPage.decreaseQuantity();
        int decreasedQuantity = Integer.parseInt(productDetailsPage.getQuantityValue());
        logger.info("Quantity Selector value after one Decrease tap: {}.", decreasedQuantity);
        CommonAssertions.verifyText(String.valueOf(decreasedQuantity), String.valueOf(initialQuantity),
                "Quantity value after one Decrease tap (expected to return to the initial value)");
        ScreenshotManager.captureScreenshot("tc011_03_quantity_after_decrease");

        // FR-011/TS-011's exact control mechanics remain Out of Current Observation at the
        // requirement level (MA-RS-001 FR-011) — this does not invent a floor-behavior business rule.
        // It only exercises the one floor behavior already documented (ProductDetailsPage class
        // Javadoc / MA-LOC-001 §8): reducing quantity to zero disables the Add To Cart button.
        productDetailsPage.decreaseQuantity();
        int floorQuantity = Integer.parseInt(productDetailsPage.getQuantityValue());
        logger.info("Quantity Selector value after decreasing below the initial value (floor probe): {}.", floorQuantity);
        ScreenshotManager.captureScreenshot("tc011_04_quantity_after_floor_decrease");

        if (floorQuantity == 0) {
            CommonAssertions.verifyDisabled(productDetailsPage.isAddToCartButtonEnabled(),
                    "Add To Cart button (quantity reduced to zero)");
            logger.info("TC-011 observed behavior confirmed: reducing quantity to zero disables the Add To Cart "
                    + "button, matching previously documented (but until now unexercised) behavior.");
        } else {
            logger.info("TC-011 observed behavior: quantity floor is {} (does not reach zero) — the documented "
                    + "zero-quantity/Add-To-Cart rule was not applicable to this run and was not asserted.", floorQuantity);
        }

        logger.info("TC-011 completed: Quantity Selector observed to accept Increase/Decrease interactions for Pilot Product '{}'.",
                pilotProduct.name());
    }

    @Test(description = "TC-013 — Product Details Scroll Support")
    public void scrollToProductHighlights() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-013: name='{}'.", pilotProduct.name());

        ProductDetailsPage productDetailsPage = navigateToProductDetails(pilotProduct, "tc013");

        String nameBeforeScroll = productDetailsPage.getProductName();
        String priceBeforeScroll = productDetailsPage.getProductPrice();
        CommonAssertions.verifyText(nameBeforeScroll, pilotProduct.name(), "Product Details — Product Name (before scroll)");

        logger.info("Scrolling to Product Highlights.");
        productDetailsPage.scrollToHighlights();
        ScreenshotManager.captureScreenshot("tc013_02_highlights_after_scroll");
        CommonAssertions.verifyVisible(productDetailsPage.isProductHighlightsDisplayed(), "Product Highlights");

        logger.info("Scrolling back to confirm Product Details remain available and no unexpected UI behavior occurred.");
        productDetailsPage.scrollToTop();
        CommonAssertions.verifyVisible(productDetailsPage.isDisplayed(), "Product Details screen (after returning to top)");
        CommonAssertions.verifyText(productDetailsPage.getProductName(), pilotProduct.name(), "Product Details — Product Name (after scroll)");
        CommonAssertions.verifyText(productDetailsPage.getProductPrice(), priceBeforeScroll, "Product Details — Product Price (after scroll)");
        ScreenshotManager.captureScreenshot("tc013_03_product_details_after_scroll_back");

        logger.info("TC-013 completed: Product Highlights revealed by scrolling, and Product Details screen remained intact and available for Pilot Product '{}'.",
                pilotProduct.name());
    }

    /**
     * Reaches the Product Details screen for {@code pilotProduct} — reuses
     * exactly the same {@code ProductsPage}/{@code ProductDetailsPage}
     * navigation methods {@link #addProductToCart()} (TC-012) already uses,
     * so TC-010/TC-011/TC-013 don't duplicate that navigation logic. Added
     * Phase 10.3.
     */
    private ProductDetailsPage navigateToProductDetails(ProductItem pilotProduct, String screenshotPrefix) {
        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen");

        String cardName = productsPage.verifyProductCardExists(pilotProduct.name());
        CommonAssertions.verifyText(cardName, pilotProduct.name(), "Product Card — Product Name");

        productsPage.openProductCard(pilotProduct.name());

        ProductDetailsPage productDetailsPage = new ProductDetailsPage();
        CommonAssertions.verifyVisible(productDetailsPage.isProductImageDisplayed(), "Product Details — Product Image");
        CommonAssertions.verifyVisible(productDetailsPage.isDisplayed(), "Product Details screen");
        ScreenshotManager.captureScreenshot(screenshotPrefix + "_01_product_details_after_navigation");

        return productDetailsPage;
    }

    /**
     * Formats {@code pilotProduct.price()} to match the AUT's on-screen
     * display convention ({@code "$ 29.99"} — dollar sign, single space, two
     * decimals) — confirmed live this session (MA-LOC-001 §6, §8, §9; the
     * same format was read on the Catalog, Product Details, and Cart
     * screens). Kept local to this test rather than added to
     * {@code ProductItem} or a Page Object, since this exact display format
     * is specific to this AUT's UI, not a general data-model concern.
     */
    private String formattedPrice(ProductItem pilotProduct) {
        return String.format("$ %.2f", pilotProduct.price());
    }
}
