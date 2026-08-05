package com.mobileautomation.framework.tests;

import com.mobileautomation.framework.assertions.CommonAssertions;
import com.mobileautomation.framework.core.BaseTest;
import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.locators.CheckoutLocators;
import com.mobileautomation.framework.locators.LoginLocators;
import com.mobileautomation.framework.models.LoginCredentials;
import com.mobileautomation.framework.models.PaymentCard;
import com.mobileautomation.framework.models.ProductItem;
import com.mobileautomation.framework.models.ShippingAddress;
import com.mobileautomation.framework.pages.CartPage;
import com.mobileautomation.framework.pages.CheckoutCompletePage;
import com.mobileautomation.framework.pages.CheckoutPage;
import com.mobileautomation.framework.pages.LoginPage;
import com.mobileautomation.framework.pages.PaymentPage;
import com.mobileautomation.framework.pages.ProductDetailsPage;
import com.mobileautomation.framework.pages.ProductsPage;
import com.mobileautomation.framework.pages.ReviewOrderPage;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import com.mobileautomation.framework.utils.WaitUtility;
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Module (MA-RS-001 §6.5). Phase 11.2 implements TC-014 and TC-017 only
 * — TC-015 (Quantity Update), TC-016 (Item Removal), and TC-018 (Proceed to
 * Checkout) are deliberately out of scope for this phase.
 * <p>
 * Each test reaches the Cart screen via {@link #addPilotProductToCartAndOpenCart(ProductItem, String)},
 * which composes {@code ProductsPage}/{@code ProductDetailsPage} exactly the
 * way {@code ProductDetailsTest.addProductToCart()} (TC-012) already does —
 * TC-012 itself is untouched, not reimplemented, not called directly (it is
 * a full Test Case with its own assertions, not a reusable navigation step).
 * {@code ExecutionStrategy.ISOLATED} resets the AUT before every test
 * method, so each test reaches the Cart independently rather than depending
 * on another test's leftover state.
 */
public class CartTest extends BaseTest {

    @Test(description = "TC-014 — Cart Screen Access")
    public void accessCartScreen() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-014: name='{}'.", pilotProduct.name());

        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, "tc014");

        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyText(cartPage.getFirstItemName(), pilotProduct.name(), "Cart line item — Product Name");
        CommonAssertions.verifyText(cartPage.getFirstItemPrice(), formattedPrice(pilotProduct), "Cart line item — Product Price");
        CommonAssertions.verifyVisible(cartPage.isTotalPriceDisplayed(), "Cart Total Price");
        CommonAssertions.verifyVisible(cartPage.isTotalItemsDisplayed(), "Cart Total Items Count");
        ScreenshotManager.captureScreenshot("tc014_02_cart_screen_verified");

        // Logged (not asserted) here — TC-014's own Expected Results only require presence, not an
        // exact value match. The raw text is logged as evidence for TC-017's exact-value assertions.
        logger.info("Cart Total Items raw text: '{}'. Cart Total Price raw text: '{}'.",
                cartPage.getTotalItems(), cartPage.getTotalPrice());

        logger.info("TC-014 completed: Cart screen displayed with correct item and total sections for Pilot Product '{}'.",
                pilotProduct.name());
    }

    @Test(description = "TC-017 — Cart Total Verification")
    public void verifyCartTotal() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-017: name='{}'.", pilotProduct.name());

        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, "tc017");

        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyVisible(cartPage.isTotalItemsDisplayed(), "Cart Total Items Count");
        CommonAssertions.verifyVisible(cartPage.isTotalPriceDisplayed(), "Cart Total Price");

        // Expected values derived at runtime from the loaded Pilot Product and the single-item Cart
        // state ExecutionStrategy.ISOLATED guarantees at the start of every test — never hardcoded.
        // The "<n> Items" format itself is confirmed real AUT behavior (TC-014 run, this session:
        // raw text read as "1 Items"), not assumed.
        String expectedTotalItems = "1 Items";
        String expectedTotalPrice = formattedPrice(pilotProduct);

        String actualTotalItems = cartPage.getTotalItems();
        String actualTotalPrice = cartPage.getTotalPrice();
        logger.info("Cart Total Items: '{}'. Cart Total Price: '{}'.", actualTotalItems, actualTotalPrice);

        CommonAssertions.verifyText(actualTotalItems, expectedTotalItems, "Cart Total Items Count");
        CommonAssertions.verifyText(actualTotalPrice, expectedTotalPrice, "Cart Total Price");
        ScreenshotManager.captureScreenshot("tc017_02_total_verified");

        logger.info("TC-017 completed: Cart Total Items ('{}') and Total Price ('{}') both verified against the Pilot Product '{}'.",
                actualTotalItems, actualTotalPrice, pilotProduct.name());
    }

    @Test(description = "TC-015 — Cart Item Quantity Update")
    public void updateCartItemQuantity() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-015: name='{}'.", pilotProduct.name());

        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, "tc015");

        // Step 5 — Verify initial quantity (read from runtime UI, never hardcoded — mirrors TC-011's
        // Phase 10.3 pattern of using the observed initial value as the base for relative assertions).
        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyVisible(cartPage.isIncreaseButtonDisplayed(), "Cart Item Increase Quantity control");
        CommonAssertions.verifyVisible(cartPage.isDecreaseButtonDisplayed(), "Cart Item Decrease Quantity control");
        int initialQuantity = Integer.parseInt(cartPage.getQuantity());
        String initialTotalItems = cartPage.getTotalItems();
        String initialTotalPrice = cartPage.getTotalPrice();
        logger.info("Cart initial state — Quantity: {}, Total Items: '{}', Total Price: '{}'.",
                initialQuantity, initialTotalItems, initialTotalPrice);
        ScreenshotManager.captureScreenshot("tc015_02_cart_initial_state");

        // Step 6 — Increase quantity by one.
        cartPage.increaseQuantity();

        // Step 7 — Verify quantity increased exactly by one.
        int increasedQuantity = Integer.parseInt(cartPage.getQuantity());
        logger.info("Cart Item quantity after Increase: {}.", increasedQuantity);
        CommonAssertions.verifyText(String.valueOf(increasedQuantity), String.valueOf(initialQuantity + 1),
                "Cart Item quantity after Increase");

        // Step 8 — Verify Total Items updated correctly (expected value = new quantity + the
        // " Items" suffix confirmed as real AUT format by TC-014's runtime evidence, Phase 11.2).
        String expectedIncreasedTotalItems = increasedQuantity + " Items";
        String actualIncreasedTotalItems = cartPage.getTotalItems();
        logger.info("Cart Total Items after Increase: '{}'.", actualIncreasedTotalItems);
        CommonAssertions.verifyText(actualIncreasedTotalItems, expectedIncreasedTotalItems, "Cart Total Items after Increase");

        // Step 9 — Verify Total Price updated correctly (unit price x quantity — the same
        // price-times-quantity relationship FR-017/TC-017 already treats as the AUT's total formula).
        String expectedIncreasedTotalPrice = formattedTotal(pilotProduct, increasedQuantity);
        String actualIncreasedTotalPrice = cartPage.getTotalPrice();
        logger.info("Cart Total Price after Increase: '{}'.", actualIncreasedTotalPrice);
        CommonAssertions.verifyText(actualIncreasedTotalPrice, expectedIncreasedTotalPrice, "Cart Total Price after Increase");
        ScreenshotManager.captureScreenshot("tc015_03_cart_after_increase");

        // Step 10 — Decrease quantity by one.
        cartPage.decreaseQuantity();

        // Step 11 — Verify quantity returned to its original value.
        int returnedQuantity = Integer.parseInt(cartPage.getQuantity());
        logger.info("Cart Item quantity after Decrease: {}.", returnedQuantity);
        CommonAssertions.verifyText(String.valueOf(returnedQuantity), String.valueOf(initialQuantity),
                "Cart Item quantity after Decrease (expected to return to the initial value)");

        // Step 12 — Verify Total Items returned correctly.
        String actualReturnedTotalItems = cartPage.getTotalItems();
        logger.info("Cart Total Items after Decrease: '{}'.", actualReturnedTotalItems);
        CommonAssertions.verifyText(actualReturnedTotalItems, initialTotalItems,
                "Cart Total Items after Decrease (expected to return to the initial value)");

        // Step 13 — Verify Total Price returned correctly.
        String actualReturnedTotalPrice = cartPage.getTotalPrice();
        logger.info("Cart Total Price after Decrease: '{}'.", actualReturnedTotalPrice);
        CommonAssertions.verifyText(actualReturnedTotalPrice, initialTotalPrice,
                "Cart Total Price after Decrease (expected to return to the initial value)");
        ScreenshotManager.captureScreenshot("tc015_04_cart_after_decrease");

        logger.info("TC-015 completed: Cart Item quantity increase/decrease correctly recalculated Total Items and Total Price for Pilot Product '{}'.",
                pilotProduct.name());
    }

    @Test(description = "TC-016 — Cart Item Removal")
    public void removeCartItem() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-016: name='{}'.", pilotProduct.name());

        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, "tc016");

        // Step 5 — Verify Product exists.
        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyVisible(cartPage.isItemPresent(), "Cart Item (before removal)");
        CommonAssertions.verifyText(cartPage.getFirstItemName(), pilotProduct.name(), "Cart line item — Product Name (before removal)");

        // Step 6 — Verify initial Quantity (read, not asserted against a hardcoded literal).
        String initialQuantity = cartPage.getQuantity();
        logger.info("Cart Item initial quantity: {}.", initialQuantity);

        // Step 7 — Verify initial Total Price.
        String initialTotalPrice = cartPage.getTotalPrice();
        logger.info("Cart initial Total Price: '{}'.", initialTotalPrice);
        CommonAssertions.verifyText(initialTotalPrice, formattedPrice(pilotProduct), "Cart Total Price (before removal)");
        ScreenshotManager.captureScreenshot("tc016_02_cart_before_removal");

        // Steps 8-9 — Remove the Product. CartPage.removeItem() waits internally on a real UI
        // condition (invisibility of the item's name element via WaitUtility, no Thread.sleep()),
        // which also absorbs the Locator Repository's documented 5-second simulated UI-thread block.
        logger.info("Removing Cart Item for Pilot Product '{}'.", pilotProduct.name());
        cartPage.removeItem();
        ScreenshotManager.captureScreenshot("tc016_03_cart_after_removal");

        // Step 10 — Verify the Product no longer exists.
        CommonAssertions.verifyHidden(cartPage.isItemPresent(), "Cart Item (after removal)");

        // Step 11 — Verify Cart becomes empty (read via the Locator Repository's own documented
        // empty-state detection element — not assumed, not invented).
        CommonAssertions.verifyVisible(cartPage.isCartEmpty(), "Cart empty-state indicator");
        String emptyCartMessage = cartPage.getEmptyCartMessage();
        logger.info("Cart empty-state message: '{}'.", emptyCartMessage);

        // Steps 12-13 — Verify Total Items/Total Price reflect the empty cart. Real-device evidence
        // (this phase) confirms both sections become hidden entirely — not zeroed-but-visible —
        // once the cart empties, consistent with them living inside the same populated-cart content
        // container as the removed item list (LOCATOR_REPOSITORY.md §9). Asserted, not assumed.
        CommonAssertions.verifyHidden(cartPage.isTotalItemsDisplayed(), "Cart Total Items Count (after removal)");
        CommonAssertions.verifyHidden(cartPage.isTotalPriceDisplayed(), "Cart Total Price (after removal)");
        ScreenshotManager.captureScreenshot("tc016_04_cart_empty_state");

        // Step 14 — Verify no application error occurs: Step 11's empty-state assertion already
        // proves this — it requires successfully reading a live element on the same Cart screen,
        // which would not succeed after a crash or an unexpected navigation away from the screen.
        logger.info("TC-016 completed: Cart Item for Pilot Product '{}' successfully removed; Cart reflects empty state (message: '{}').",
                pilotProduct.name(), emptyCartMessage);
    }

    /**
     * TC-018 — Proceed to Checkout Action (Anonymous User). Implements
     * exactly the runtime rule Phase 11.7 verified on the real device: an
     * anonymous user tapping Proceed To Checkout is redirected to the Login
     * screen; a successful login then automatically resumes the interrupted
     * checkout flow, landing directly on the Shipping Address screen — no
     * other navigation is assumed.
     */
    @Test(description = "TC-018 — Proceed to Checkout Action (Anonymous User)")
    public void proceedToCheckoutAnonymousUser() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-018 (anonymous flow): name='{}'.", pilotProduct.name());
        logger.info("Authentication state: anonymous — ExecutionStrategy.ISOLATED guarantees a logged-out starting state.");

        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, "tc018a");

        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyVisible(cartPage.isProceedToCheckoutButtonDisplayed(), "Proceed To Checkout button");
        CommonAssertions.verifyEnabled(cartPage.isProceedToCheckoutButtonEnabled(), "Proceed To Checkout button");
        ScreenshotManager.captureScreenshot("tc018a_02_cart_before_checkout");

        logger.info("Tapping Proceed To Checkout (anonymous).");
        cartPage.tapProceedToCheckout();

        String destination = waitForLoginOrShipping();
        logger.info("Detected destination screen after Proceed To Checkout: {}.", destination);
        CommonAssertions.verifyText(destination, "LOGIN",
                "Checkout destination screen (anonymous user — verified runtime rule, Phase 11.7: redirected to Login first)");

        LoginPage loginPage = new LoginPage();
        ScreenshotManager.captureScreenshot("tc018a_03_login_screen");

        LoginCredentials credentials = TestDataManager.getInstance().loginData().standardCredentials();
        logger.info("Authenticating to resume the interrupted checkout flow.");
        loginPage.login(credentials.username(), credentials.password());

        String postLoginDestination = waitForShipping();
        logger.info("Post-login destination screen: {}.", postLoginDestination);
        CommonAssertions.verifyText(postLoginDestination, "SHIPPING",
                "Checkout destination screen after successful login (verified runtime rule, Phase 11.7: automatically resumes to Shipping Address)");

        CheckoutPage checkoutPage = new CheckoutPage();
        verifyShippingScreen(checkoutPage, pilotProduct);
        ScreenshotManager.captureScreenshot("tc018a_04_shipping_address_screen");

        logger.info("TC-018 (Anonymous User) completed: Proceed To Checkout redirected to Login; successful login auto-resumed to Shipping Address.");
    }

    /**
     * TC-018 — Proceed to Checkout Action (Authenticated User). Implements
     * the second runtime rule Phase 11.7 verified: an already-authenticated
     * user tapping Proceed To Checkout goes directly to the Shipping Address
     * screen — no Login interstitial. Logs in first (reusing the same
     * Navigation Drawer flow {@code LoginTest.loginOutcomeVerification()}
     * already proves, TC-004), then reuses {@link #addPilotProductToCartAndOpenCart}
     * exactly as every other Cart Test Case does.
     */
    @Test(description = "TC-018 — Proceed to Checkout Action (Authenticated User)")
    public void proceedToCheckoutAuthenticatedUser() {
        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (post-launch)");
        logger.info("Authentication state: anonymous (pre-login) — logging in before Add to Cart, per TC-004's proven flow.");

        productsPage.tapMenu();
        productsPage.tapDrawerItem("Log In");

        LoginPage loginPage = new LoginPage();
        CommonAssertions.verifyVisible(loginPage.isDisplayed(), "Login screen");
        LoginCredentials credentials = TestDataManager.getInstance().loginData().standardCredentials();
        loginPage.login(credentials.username(), credentials.password());
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (post-login)");
        logger.info("Authentication state: authenticated.");

        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-018 (authenticated flow): name='{}'.", pilotProduct.name());

        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, "tc018b");

        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyVisible(cartPage.isProceedToCheckoutButtonDisplayed(), "Proceed To Checkout button");
        CommonAssertions.verifyEnabled(cartPage.isProceedToCheckoutButtonEnabled(), "Proceed To Checkout button");
        ScreenshotManager.captureScreenshot("tc018b_02_cart_before_checkout");

        logger.info("Tapping Proceed To Checkout (authenticated).");
        cartPage.tapProceedToCheckout();

        String destination = waitForLoginOrShipping();
        logger.info("Detected destination screen after Proceed To Checkout: {}.", destination);
        CommonAssertions.verifyText(destination, "SHIPPING",
                "Checkout destination screen (authenticated user — verified runtime rule, Phase 11.7: goes directly to Shipping Address)");

        CheckoutPage checkoutPage = new CheckoutPage();
        verifyShippingScreen(checkoutPage, pilotProduct);
        ScreenshotManager.captureScreenshot("tc018b_03_shipping_address_screen");

        logger.info("TC-018 (Authenticated User) completed: Proceed To Checkout navigated directly to Shipping Address, no Login interstitial.");
    }

    /**
     * TC-019 — Shipping Screen Access. Reaches the Shipping Address screen
     * via {@link #navigateToShippingScreen(ProductItem, String)} (reusing
     * TC-018's proven, conditional-Login-aware navigation), then verifies
     * the title, subtitle, and each of the seven fields individually —
     * never relying only on the aggregate {@code areAllFieldsDisplayed()}
     * check, so a failure pinpoints exactly which field is missing.
     */
    @Test(description = "TC-019 — Shipping Screen Access")
    public void accessShippingScreen() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-019: name='{}'.", pilotProduct.name());

        CheckoutPage checkoutPage = navigateToShippingScreen(pilotProduct, "tc019");
        ScreenshotManager.captureScreenshot("tc019_02_shipping_screen_reached");

        CommonAssertions.verifyVisible(checkoutPage.isDisplayed(), "Checkout title");
        CommonAssertions.verifyVisible(checkoutPage.isSubtitleDisplayed(), "Shipping subtitle");
        CommonAssertions.verifyVisible(checkoutPage.isFullNameFieldDisplayed(), "Full Name field");
        CommonAssertions.verifyVisible(checkoutPage.isAddressLine1FieldDisplayed(), "Address Line 1 field");
        CommonAssertions.verifyVisible(checkoutPage.isAddressLine2FieldDisplayed(), "Address Line 2 field");
        CommonAssertions.verifyVisible(checkoutPage.isCityFieldDisplayed(), "City field");
        CommonAssertions.verifyVisible(checkoutPage.isStateFieldDisplayed(), "State/Region field");
        CommonAssertions.verifyVisible(checkoutPage.isZipCodeFieldDisplayed(), "Zip Code field");
        CommonAssertions.verifyVisible(checkoutPage.isCountryFieldDisplayed(), "Country field");
        ScreenshotManager.captureScreenshot("tc019_03_all_fields_individually_verified");

        logger.info("TC-019 completed: Shipping Address screen displayed with all seven fields individually verified.");
    }

    /**
     * TC-020 — Shipping Address Data Entry. Reaches the Shipping Address
     * screen the same way as TC-019, enters TD-002's standard dataset
     * (never hardcoded — read via {@code TestDataManager}), and reads each
     * field back individually to verify it retained the entered value.
     */
    @Test(description = "TC-020 — Shipping Address Data Entry")
    public void enterShippingAddressData() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-020: name='{}'.", pilotProduct.name());

        CheckoutPage checkoutPage = navigateToShippingScreen(pilotProduct, "tc020");
        ScreenshotManager.captureScreenshot("tc020_02_shipping_screen_before_entry");

        ShippingAddress address = TestDataManager.getInstance().shippingData().standardAddress();
        logger.info("Loaded Shipping Address test data (TD-002): fullName='{}', city='{}', country='{}'.",
                address.fullName(), address.city(), address.country());

        checkoutPage.enterShippingAddress(address);
        ScreenshotManager.captureScreenshot("tc020_03_shipping_screen_after_entry");

        CommonAssertions.verifyText(checkoutPage.getFullName(), address.fullName(), "Full Name field value");
        CommonAssertions.verifyText(checkoutPage.getAddressLine1(), address.addressLine1(), "Address Line 1 field value");
        CommonAssertions.verifyText(checkoutPage.getAddressLine2(), address.addressLine2(), "Address Line 2 field value");
        CommonAssertions.verifyText(checkoutPage.getCity(), address.city(), "City field value");
        CommonAssertions.verifyText(checkoutPage.getState(), address.state(), "State/Region field value");
        CommonAssertions.verifyText(checkoutPage.getZipCode(), address.zipCode(), "Zip Code field value");
        CommonAssertions.verifyText(checkoutPage.getCountry(), address.country(), "Country field value");

        logger.info("TC-020 completed: all seven Shipping Address fields entered and individually verified against TD-002.");
    }

    /**
     * TC-021 — Proceed to Payment Action. Reaches the Shipping Address
     * screen the same way as TC-019/020, then populates it (reusing
     * {@code CheckoutPage.enterShippingAddress()} and TD-002, exactly as
     * TC-020 does) before tapping To Payment — this is not optional
     * decoration: TS-021's own documented Precondition is "Shipping Address
     * fields are populated", and real-device evidence (this phase's first
     * run) confirmed why — tapping To Payment against the AUT's own
     * client-side validation on an empty form blocks navigation entirely
     * and surfaces "Please provide your..." field errors instead. Verifies
     * the button, taps it, and confirms a successful transition away from
     * Shipping. Deliberately does not reference, validate, or assert
     * anything about the Payment screen itself — that is TC-022/TC-023's
     * scope, not this phase's.
     */
    @Test(description = "TC-021 — Proceed to Payment Action")
    public void proceedToPayment() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-021: name='{}'.", pilotProduct.name());

        CheckoutPage checkoutPage = navigateToShippingScreen(pilotProduct, "tc021");

        ShippingAddress address = TestDataManager.getInstance().shippingData().standardAddress();
        logger.info("Populating Shipping Address fields (TS-021 Precondition) before Proceed to Payment.");
        checkoutPage.enterShippingAddress(address);
        ScreenshotManager.captureScreenshot("tc021_02_shipping_screen_fields_populated");

        CommonAssertions.verifyVisible(checkoutPage.isToPaymentButtonDisplayed(), "To Payment button");
        CommonAssertions.verifyEnabled(checkoutPage.isToPaymentButtonEnabled(), "To Payment button");
        ScreenshotManager.captureScreenshot("tc021_03_shipping_screen_before_to_payment");

        logger.info("Tapping To Payment.");
        boolean navigatedAwayFromShipping = checkoutPage.tapToPayment();
        ScreenshotManager.captureScreenshot("tc021_04_after_to_payment_navigation");

        CommonAssertions.verifyVisible(navigatedAwayFromShipping,
                "Successful transition away from Shipping Address screen (To Payment tap) — Payment screen itself not validated, out of this phase's scope");

        logger.info("TC-021 completed: Shipping Address populated, To Payment button tapped successfully, navigation away from Shipping Address confirmed.");
    }

    /**
     * TC-022 — Payment Screen Access. Extends TC-021's own proven
     * navigation (populate Shipping Address, tap To Payment) one step
     * further onto the Payment screen itself, then verifies every static
     * element individually — the same non-aggregate-only discipline TC-019
     * established for the Shipping Address screen, so a failure pinpoints
     * exactly which element is missing. Uses only what Phase 12.3's
     * real-device investigation verified: no typing into the Cardholder
     * Name, Card Number, Expiration Date, or Security Code fields, no
     * interaction with the Billing-Same-As-Shipping checkbox, and no tap on
     * Review Order — those remain TC-023's scope, not this test's.
     */
    @Test(description = "TC-022 — Payment Screen Access")
    public void accessPaymentScreen() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-022: name='{}'.", pilotProduct.name());

        CheckoutPage checkoutPage = navigateToShippingScreen(pilotProduct, "tc022");

        ShippingAddress address = TestDataManager.getInstance().shippingData().standardAddress();
        logger.info("Populating Shipping Address fields (precondition shared with TC-021) before Proceed to Payment.");
        checkoutPage.enterShippingAddress(address);
        ScreenshotManager.captureScreenshot("tc022_02_shipping_screen_fields_populated");

        CommonAssertions.verifyVisible(checkoutPage.isToPaymentButtonDisplayed(), "To Payment button");
        CommonAssertions.verifyEnabled(checkoutPage.isToPaymentButtonEnabled(), "To Payment button");

        logger.info("Tapping To Payment.");
        boolean navigatedAwayFromShipping = checkoutPage.tapToPayment();
        CommonAssertions.verifyVisible(navigatedAwayFromShipping,
                "Successful transition away from Shipping Address screen (To Payment tap)");
        ScreenshotManager.captureScreenshot("tc022_03_payment_screen_arrival");

        PaymentPage paymentPage = new PaymentPage();
        CommonAssertions.verifyVisible(paymentPage.isPaymentScreenDisplayed(), "Payment screen (arrival)");

        CommonAssertions.verifyVisible(paymentPage.isPaymentTitleDisplayed(), "Payment screen — title");
        CommonAssertions.verifyVisible(paymentPage.isPaymentSubtitleDisplayed(), "Payment screen — \"Enter a payment method\" subtitle");
        CommonAssertions.verifyVisible(paymentPage.isPaymentDetailsNoteDisplayed(), "Payment screen — payment details note");
        CommonAssertions.verifyVisible(paymentPage.isCardLabelDisplayed(), "Payment screen — Card label");
        CommonAssertions.verifyVisible(paymentPage.isVisaIconDisplayed(), "Payment screen — Visa icon");
        CommonAssertions.verifyVisible(paymentPage.isMastercardIconDisplayed(), "Payment screen — Mastercard icon");
        CommonAssertions.verifyVisible(paymentPage.isCardholderNameLabelDisplayed(), "Payment screen — Cardholder Name label");
        CommonAssertions.verifyVisible(paymentPage.isCardholderNameFieldDisplayed(), "Payment screen — Cardholder Name field");
        CommonAssertions.verifyVisible(paymentPage.isCardNumberLabelDisplayed(), "Payment screen — Card Number label");
        CommonAssertions.verifyVisible(paymentPage.isCardNumberFieldDisplayed(), "Payment screen — Card Number field");
        CommonAssertions.verifyVisible(paymentPage.isExpirationDateLabelDisplayed(), "Payment screen — Expiration Date label");
        CommonAssertions.verifyVisible(paymentPage.isExpirationDateFieldDisplayed(), "Payment screen — Expiration Date field");
        CommonAssertions.verifyVisible(paymentPage.isSecurityCodeLabelDisplayed(), "Payment screen — Security Code label");
        CommonAssertions.verifyVisible(paymentPage.isSecurityCodeFieldDisplayed(), "Payment screen — Security Code field");
        CommonAssertions.verifyVisible(paymentPage.isBillingCheckboxDisplayed(), "Payment screen — Billing-Same-As-Shipping checkbox");
        CommonAssertions.verifyVisible(paymentPage.isReviewOrderButtonDisplayed(), "Payment screen — Review Order button");
        ScreenshotManager.captureScreenshot("tc022_04_all_elements_individually_verified");

        logger.info("TC-022 completed: Payment screen reached and all static elements individually verified.");
    }

    /**
     * TC-023 — Payment Card Data Entry. Extends TC-022's own proven
     * navigation (populate Shipping Address, tap To Payment, reach the
     * Payment screen) one step further: enters TD-003's standard dataset
     * into all four Payment Card fields and reads each back individually to
     * verify it retained the entered value — the same pattern TC-020
     * established for the Shipping Address fields. Card Number and
     * Expiration Date are asserted against their AUT-auto-formatted
     * expected values (space-grouped digits, slash-inserted date), not the
     * raw typed input, per Phase 12.5's confirmed runtime evidence that
     * {@code getText()} returns the formatted value for these two custom
     * widgets. Also confirms the Billing-Same-As-Shipping checkbox remains
     * checked by default. Does not toggle the checkbox (Phase 12.5 already
     * investigated the toggled-off, duplicate-locator state directly) and
     * does not tap Review Order — that remains out of this test's scope.
     */
    @Test(description = "TC-023 — Payment Card Data Entry")
    public void enterPaymentCardData() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-023: name='{}'.", pilotProduct.name());

        CheckoutPage checkoutPage = navigateToShippingScreen(pilotProduct, "tc023");

        ShippingAddress address = TestDataManager.getInstance().shippingData().standardAddress();
        logger.info("Populating Shipping Address fields (precondition shared with TC-021/022) before Proceed to Payment.");
        checkoutPage.enterShippingAddress(address);

        CommonAssertions.verifyVisible(checkoutPage.isToPaymentButtonDisplayed(), "To Payment button");
        CommonAssertions.verifyEnabled(checkoutPage.isToPaymentButtonEnabled(), "To Payment button");

        logger.info("Tapping To Payment.");
        boolean navigatedAwayFromShipping = checkoutPage.tapToPayment();
        CommonAssertions.verifyVisible(navigatedAwayFromShipping,
                "Successful transition away from Shipping Address screen (To Payment tap)");
        ScreenshotManager.captureScreenshot("tc023_02_payment_screen_arrival");

        PaymentPage paymentPage = new PaymentPage();
        CommonAssertions.verifyVisible(paymentPage.isPaymentScreenDisplayed(), "Payment screen (arrival)");

        CommonAssertions.verifyVisible(paymentPage.isBillingCheckboxChecked(),
                "Billing-Same-As-Shipping checkbox (default state, Phase 12.3/12.5 confirmed checked by default)");

        PaymentCard card = TestDataManager.getInstance().paymentData().standardCard();
        logger.info("Loaded Payment Card test data (TD-003): cardholderName='{}'.", card.cardholderName());

        paymentPage.enterPaymentCard(card);
        ScreenshotManager.captureScreenshot("tc023_03_payment_card_entered");

        CommonAssertions.verifyText(paymentPage.getCardholderName(), card.cardholderName(), "Cardholder Name field value");
        CommonAssertions.verifyText(paymentPage.getCardNumber(), card.expectedCardNumber(), "Card Number field value (AUT auto-formatted)");
        CommonAssertions.verifyText(paymentPage.getExpirationDate(), card.expectedExpirationDate(), "Expiration Date field value (AUT auto-formatted)");
        CommonAssertions.verifyText(paymentPage.getSecurityCode(), card.securityCode(), "Security Code field value");

        logger.info("TC-023 completed: Payment Card fields entered and individually verified, including AUT auto-formatting for Card Number and Expiration Date.");
    }

    /**
     * TC-026 — Place Order. Extends TC-023's own proven navigation
     * (populate Shipping Address, tap To Payment, populate Payment Card)
     * one step further: taps Review Order to reach the Review Order
     * screen, verifies every business section individually, taps Place
     * Order, verifies the Checkout Complete screen individually, taps
     * Continue Shopping, and confirms the app returns to a normal,
     * functional Product Catalog state. Uses the runtime evidence already
     * documented in MA-LOC-001 §12/§13 (source-decompiled), corroborated by
     * this phase's own real-device execution.
     * <p>
     * <b>Cart Badge, corrected during this phase's own runtime validation:</b>
     * the first implementation attempt asserted the badge remains visible on
     * the Products screen after Continue Shopping, extending MA-LOC-001
     * §13's claim that the badge does not reset <em>while still on the
     * Checkout Complete screen</em>. Real-device evidence (this phase)
     * showed that extension was wrong — the badge is absent on the Products
     * screen once Continue Shopping is tapped. The two behaviors are
     * distinct; this test now asserts only what was actually observed, not
     * the initial, unsupported inference. The Review Order screen was also
     * found to be a single scrollable page taller than the viewport —
     * {@code ReviewOrderPage} scrolls to each below-the-fold section before
     * asserting it (see that class's Javadoc).
     */
    @Test(description = "TC-026 — Place Order")
    public void placeOrder() {
        ProductItem pilotProduct = TestDataManager.getInstance().productData().pilotProduct();
        logger.info("Loaded Pilot Product for TC-026: name='{}'.", pilotProduct.name());

        CheckoutPage checkoutPage = navigateToShippingScreen(pilotProduct, "tc026");

        ShippingAddress address = TestDataManager.getInstance().shippingData().standardAddress();
        logger.info("Populating Shipping Address fields.");
        checkoutPage.enterShippingAddress(address);

        CommonAssertions.verifyVisible(checkoutPage.isToPaymentButtonDisplayed(), "To Payment button");
        CommonAssertions.verifyEnabled(checkoutPage.isToPaymentButtonEnabled(), "To Payment button");

        logger.info("Tapping To Payment.");
        boolean navigatedToPayment = checkoutPage.tapToPayment();
        CommonAssertions.verifyVisible(navigatedToPayment, "Successful transition to Payment screen");

        PaymentPage paymentPage = new PaymentPage();
        CommonAssertions.verifyVisible(paymentPage.isPaymentScreenDisplayed(), "Payment screen (arrival)");

        PaymentCard card = TestDataManager.getInstance().paymentData().standardCard();
        logger.info("Populating Payment Card fields.");
        paymentPage.enterPaymentCard(card);
        ScreenshotManager.captureScreenshot("tc026_02_payment_card_entered");

        CommonAssertions.verifyVisible(paymentPage.isReviewOrderButtonDisplayed(), "Review Order button");
        CommonAssertions.verifyEnabled(paymentPage.isReviewOrderButtonEnabled(), "Review Order button");

        logger.info("Tapping Review Order.");
        boolean navigatedToReviewOrder = paymentPage.tapReviewOrder();
        CommonAssertions.verifyVisible(navigatedToReviewOrder, "Successful transition to Review Order screen");
        ScreenshotManager.captureScreenshot("tc026_03_review_order_screen_arrival");

        ReviewOrderPage reviewOrderPage = new ReviewOrderPage();
        CommonAssertions.verifyVisible(reviewOrderPage.isCheckoutTitleDisplayed(), "Review Order screen — Checkout title");
        CommonAssertions.verifyVisible(reviewOrderPage.isReviewOrderSubtitleDisplayed(), "Review Order screen — \"Review your order\" title");
        CommonAssertions.verifyText(reviewOrderPage.getFirstProductName(), pilotProduct.name(), "Review Order screen — Product information (name)");
        CommonAssertions.verifyText(reviewOrderPage.getFirstProductPrice(), formattedPrice(pilotProduct), "Review Order screen — Product information (price)");
        CommonAssertions.verifyVisible(reviewOrderPage.isDeliveryAddressDisplayed(), "Review Order screen — Delivery Address section");
        CommonAssertions.verifyText(reviewOrderPage.getDeliveryAddressFullName(), address.fullName(), "Review Order screen — Delivery Address full name");
        CommonAssertions.verifyVisible(reviewOrderPage.isPaymentMethodDisplayed(), "Review Order screen — Payment Method section");
        CommonAssertions.verifyText(reviewOrderPage.getPaymentMethodCardholderName(), card.cardholderName(), "Review Order screen — Payment Method cardholder name");
        CommonAssertions.verifyVisible(reviewOrderPage.isBillingAddressSameNoteDisplayed(), "Review Order screen — Billing Address section");
        CommonAssertions.verifyVisible(reviewOrderPage.isShippingMethodDisplayed(), "Review Order screen — Shipping Method section");
        CommonAssertions.verifyVisible(reviewOrderPage.isTotalItemsCountDisplayed(), "Review Order screen — Total (item count)");
        CommonAssertions.verifyVisible(reviewOrderPage.isTotalAmountDisplayed(), "Review Order screen — Total (amount)");
        ScreenshotManager.captureScreenshot("tc026_04_review_order_screen_verified");

        CommonAssertions.verifyVisible(reviewOrderPage.isPlaceOrderButtonDisplayed(), "Place Order button");
        CommonAssertions.verifyEnabled(reviewOrderPage.isPlaceOrderButtonEnabled(), "Place Order button");
        ScreenshotManager.captureScreenshot("tc026_05_before_place_order");

        logger.info("Tapping Place Order.");
        boolean navigatedToCheckoutComplete = reviewOrderPage.tapPlaceOrder();
        CommonAssertions.verifyVisible(navigatedToCheckoutComplete, "Successful transition to Checkout Complete screen");
        ScreenshotManager.captureScreenshot("tc026_06_checkout_complete_arrival");

        CheckoutCompletePage checkoutCompletePage = new CheckoutCompletePage();
        CommonAssertions.verifyVisible(checkoutCompletePage.isCheckoutCompleteTitleDisplayed(), "Checkout Complete screen — title");
        CommonAssertions.verifyVisible(checkoutCompletePage.isThankYouMessageDisplayed(), "Checkout Complete screen — Thank You message");
        CommonAssertions.verifyVisible(checkoutCompletePage.isSuccessMessageDisplayed(), "Checkout Complete screen — Success message");
        CommonAssertions.verifyVisible(checkoutCompletePage.isDispatchMessageDisplayed(), "Checkout Complete screen — Dispatch message");
        CommonAssertions.verifyVisible(checkoutCompletePage.isContinueShoppingButtonDisplayed(), "Checkout Complete screen — Continue Shopping button");
        CommonAssertions.verifyEnabled(checkoutCompletePage.isContinueShoppingButtonEnabled(), "Checkout Complete screen — Continue Shopping button");
        ScreenshotManager.captureScreenshot("tc026_07_checkout_complete_verified");

        logger.info("Tapping Continue Shopping.");
        checkoutCompletePage.tapContinueShopping();

        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after Continue Shopping)");
        CommonAssertions.verifyHidden(productsPage.isCartBadgeDisplayed(),
                "Cart Badge (confirmed this phase, real-device evidence: the badge is absent on the Products screen after Continue Shopping — "
                        + "distinct from MA-LOC-001 §13's own, narrower claim that the badge does not reset while still on the Checkout Complete screen itself; "
                        + "this test does not assume the two behaviors are the same, and real-device evidence shows they are not)");
        ScreenshotManager.captureScreenshot("tc026_08_products_screen_after_continue_shopping");

        logger.info("TC-026 completed: full checkout flow from Shipping through Place Order to Checkout Complete and back to Products screen.");
    }

    /**
     * TC-006 — Product Sort Interaction. Verifies all four Sort dialog
     * options (MA-LOC-001 §7: Name - Ascending, Name - Descending, Price -
     * Ascending, Price - Descending) individually — option displayed,
     * selection succeeds (its checkmark becomes the active one), the
     * product list actually refreshes, and the resulting order is
     * genuinely correct, not merely that a selection was registered.
     * Ordering correctness is verified by a pairwise comparison across
     * every product currently rendered by {@code ProductsPage.getProductCount()}/
     * {@code getProductName(int)}/{@code getProductPrice(int)} after each
     * selection — never a hardcoded expected product list, since the
     * Product Catalog's exact contents are AUT data owned by Sauce Labs,
     * not test data this framework defines (the same reasoning
     * {@code ProductDataFactory} already applies: only the Pilot Product
     * identity is test data, never the full catalog). This necessarily
     * verifies ordering across the initially-rendered viewport (confirmed
     * real-device evidence, Phase 14.1: every sort selection scrolls the
     * catalog back to its top), not a full scroll-and-collect of the whole
     * catalog.
     * <p>
     * <b>Real-device evidence (Phase 14.1 runtime investigation):</b> Name
     * - Ascending is the Product Catalog's default sort state on a fresh
     * app launch — its checkmark is already visible with no interaction,
     * consistent with MA-LOC-001 §7 already documenting the other three
     * checkmarks (but not this one) as {@code invisible} by default. This
     * test captures that default order as a baseline, cycles through the
     * other three options, then explicitly re-selects Name - Ascending and
     * asserts the catalog returns to that exact baseline — confirming the
     * explicit selection and the default state produce identical results,
     * not merely similar ones. It also confirms tapping the already-active
     * option again safely dismisses the dialog without changing the order.
     * <p>
     * FR-006/TS-006/TC-006 in MA-TC-001 currently document the resulting
     * catalog re-ordering behavior as "Out of Current Observation" — this
     * phase's scope is implementation only (Documentation Reconciliation is
     * explicitly out of scope), so that documentation is intentionally left
     * unchanged here despite this test now resolving it with real evidence.
     */
    @Test(description = "TC-006 — Product Sort Interaction")
    public void sortProductCatalog() {
        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen");

        List<String> defaultOrder = currentProductNames(productsPage);
        logger.info("Default (pre-interaction) Product Catalog order (names): {}.", defaultOrder);
        ScreenshotManager.captureScreenshot("tc006_01_catalog_default_order");

        // Step 1 — Tap the Sort control. Expected Result: Sort control responds to activation.
        productsPage.tapSort();
        CommonAssertions.verifyVisible(productsPage.isSortDialogTitleDisplayed(), "Sort dialog — title");
        ScreenshotManager.captureScreenshot("tc006_02_sort_dialog_opened");

        CommonAssertions.verifyVisible(productsPage.isSortNameAscendingOptionDisplayed(), "Sort dialog — Name - Ascending option");
        CommonAssertions.verifyVisible(productsPage.isSortNameDescendingOptionDisplayed(), "Sort dialog — Name - Descending option");
        CommonAssertions.verifyVisible(productsPage.isSortPriceAscendingOptionDisplayed(), "Sort dialog — Price - Ascending option");
        CommonAssertions.verifyVisible(productsPage.isSortPriceDescendingOptionDisplayed(), "Sort dialog — Price - Descending option");
        CommonAssertions.verifyVisible(productsPage.isSortNameAscendingSelected(), "Sort dialog — Name - Ascending checkmark (default state)");

        // Step 2 (Name - Descending) — Observe the Product Catalog screen after Sort control activation.
        logger.info("Selecting Name - Descending.");
        productsPage.tapSortNameDescending();
        ScreenshotManager.captureScreenshot("tc006_03_name_descending_selected");
        List<String> nameDescendingOrder = currentProductNames(productsPage);
        logger.info("Product Catalog order after Name - Descending: {}.", nameDescendingOrder);
        CommonAssertions.verifyVisible(!nameDescendingOrder.equals(defaultOrder), "Product Catalog list refresh after Name - Descending selection");
        verifyNameOrderDescending(nameDescendingOrder, "Name - Descending");

        productsPage.tapSort();
        CommonAssertions.verifyVisible(productsPage.isSortNameDescendingSelected(), "Sort dialog — Name - Descending checkmark (after selection)");

        // Price - Ascending
        logger.info("Selecting Price - Ascending.");
        productsPage.tapSortPriceAscending();
        ScreenshotManager.captureScreenshot("tc006_04_price_ascending_selected");
        List<String> priceAscendingNames = currentProductNames(productsPage);
        List<BigDecimal> priceAscendingPrices = currentProductPrices(productsPage);
        logger.info("Product Catalog order after Price - Ascending (prices): {}.", priceAscendingPrices);
        CommonAssertions.verifyVisible(!priceAscendingNames.equals(nameDescendingOrder), "Product Catalog list refresh after Price - Ascending selection");
        verifyPriceOrderAscending(priceAscendingPrices, "Price - Ascending");

        productsPage.tapSort();
        CommonAssertions.verifyVisible(productsPage.isSortPriceAscendingSelected(), "Sort dialog — Price - Ascending checkmark (after selection)");

        // Price - Descending
        logger.info("Selecting Price - Descending.");
        productsPage.tapSortPriceDescending();
        ScreenshotManager.captureScreenshot("tc006_05_price_descending_selected");
        List<String> priceDescendingNames = currentProductNames(productsPage);
        List<BigDecimal> priceDescendingPrices = currentProductPrices(productsPage);
        logger.info("Product Catalog order after Price - Descending (prices): {}.", priceDescendingPrices);
        CommonAssertions.verifyVisible(!priceDescendingNames.equals(priceAscendingNames), "Product Catalog list refresh after Price - Descending selection");
        verifyPriceOrderDescending(priceDescendingPrices, "Price - Descending");

        productsPage.tapSort();
        CommonAssertions.verifyVisible(productsPage.isSortPriceDescendingSelected(), "Sort dialog — Price - Descending checkmark (after selection)");

        // Name - Ascending (explicit re-selection) — must return to the captured default baseline.
        logger.info("Re-selecting Name - Ascending.");
        productsPage.tapSortNameAscending();
        ScreenshotManager.captureScreenshot("tc006_06_name_ascending_reselected");
        List<String> reselectedOrder = currentProductNames(productsPage);
        logger.info("Product Catalog order after explicitly re-selecting Name - Ascending: {}.", reselectedOrder);
        CommonAssertions.verifyText(String.join("|", reselectedOrder), String.join("|", defaultOrder),
                "Product Catalog order after explicitly re-selecting Name - Ascending (expected to match the captured default baseline)");
        verifyNameOrderAscending(reselectedOrder, "Name - Ascending");

        productsPage.tapSort();
        CommonAssertions.verifyVisible(productsPage.isSortNameAscendingSelected(), "Sort dialog — Name - Ascending checkmark (after explicit re-selection)");

        // Confirms tapping the already-active option is safe: dismisses the dialog without changing the order.
        logger.info("Tapping the already-active Name - Ascending option again to dismiss the dialog.");
        productsPage.tapSortNameAscending();
        ScreenshotManager.captureScreenshot("tc006_07_dialog_dismissed_no_change");
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after dismissing Sort dialog via already-active option)");
        List<String> finalOrder = currentProductNames(productsPage);
        CommonAssertions.verifyText(String.join("|", finalOrder), String.join("|", defaultOrder),
                "Product Catalog order after tapping the already-active Name - Ascending option again (expected unchanged)");

        logger.info("TC-006 completed: all four Sort dialog options individually verified — option displayed, selection succeeded, "
                + "product list refreshed, and resulting order correctness confirmed for Name - Ascending/Descending and Price - Ascending/Descending.");
    }

    /**
     * Reaches the Shipping Address screen for {@code pilotProduct} — reuses
     * {@link #addPilotProductToCartAndOpenCart(ProductItem, String)} and the
     * same conditional Login/Shipping branch logic {@link #proceedToCheckoutAnonymousUser()}
     * / {@link #proceedToCheckoutAuthenticatedUser()} (TC-018) established,
     * via the shared {@link #waitForLoginOrShipping()}/{@link #waitForShipping()}
     * helpers, so TC-019/020/021 don't duplicate that navigation or branch
     * logic. TC-018 itself is not called or reimplemented.
     */
    private CheckoutPage navigateToShippingScreen(ProductItem pilotProduct, String screenshotPrefix) {
        CartPage cartPage = addPilotProductToCartAndOpenCart(pilotProduct, screenshotPrefix);

        CommonAssertions.verifyVisible(cartPage.isDisplayed(), "Cart screen");
        CommonAssertions.verifyVisible(cartPage.isProceedToCheckoutButtonDisplayed(), "Proceed To Checkout button");
        CommonAssertions.verifyEnabled(cartPage.isProceedToCheckoutButtonEnabled(), "Proceed To Checkout button");

        logger.info("Tapping Proceed To Checkout.");
        cartPage.tapProceedToCheckout();

        String destination = waitForLoginOrShipping();
        logger.info("Detected destination screen after Proceed To Checkout: {}.", destination);

        if ("LOGIN".equals(destination)) {
            logger.info("Anonymous state detected — authenticating to resume the interrupted checkout flow.");
            LoginPage loginPage = new LoginPage();
            LoginCredentials credentials = TestDataManager.getInstance().loginData().standardCredentials();
            loginPage.login(credentials.username(), credentials.password());

            String postLoginDestination = waitForShipping();
            CommonAssertions.verifyText(postLoginDestination, "SHIPPING",
                    "Checkout destination screen after successful login (verified runtime rule, Phase 11.7: automatically resumes to Shipping Address)");
        } else {
            CommonAssertions.verifyText(destination, "SHIPPING",
                    "Checkout destination screen (verified runtime rule, Phase 11.7)");
        }

        CheckoutPage checkoutPage = new CheckoutPage();
        CommonAssertions.verifyVisible(checkoutPage.isDisplayed(), "Shipping Address screen title");
        return checkoutPage;
    }

    /**
     * Waits for either the Login screen or the Shipping Address screen to
     * appear — the two, and only the two, destinations Phase 11.7 verified
     * on the real device — using {@code WaitUtility}'s existing custom-wait
     * escape hatch (no new WaitUtility capability, no sleep). Queries the
     * driver directly rather than through a Page Object's {@code isDisplayed()},
     * since that method's own internal wait would make a non-blocking
     * presence check impossible inside this polling loop. Returns
     * {@code "UNKNOWN"} — never throws — if neither appears within the
     * explicit-wait timeout, so the caller's own {@code CommonAssertions}
     * call produces a clear, reported business-assertion failure instead of
     * an unhandled framework exception.
     */
    private String waitForLoginOrShipping() {
        try {
            return WaitUtility.waitUntil(driver -> {
                if (!driver.findElements(LoginLocators.LOGIN_TITLE).isEmpty()) {
                    return "LOGIN";
                }
                if (!driver.findElements(CheckoutLocators.SHIPPING_TITLE).isEmpty()) {
                    return "SHIPPING";
                }
                return null;
            });
        } catch (TimeoutException e) {
            logger.error("Neither the Login screen nor the Shipping Address screen appeared after tapping Proceed To Checkout within the explicit-wait timeout.", e);
            return "UNKNOWN";
        }
    }

    /** Same escape-hatch pattern as {@link #waitForLoginOrShipping()}, narrowed to only the Shipping Address screen — used after a successful login to confirm the auto-resume Phase 11.7 verified. */
    private String waitForShipping() {
        try {
            return WaitUtility.waitUntil(driver -> !driver.findElements(CheckoutLocators.SHIPPING_TITLE).isEmpty() ? "SHIPPING" : null);
        } catch (TimeoutException e) {
            logger.error("Shipping Address screen did not appear after login within the explicit-wait timeout.", e);
            return "UNKNOWN";
        }
    }

    /**
     * Verifies the Shipping Address screen's title, subtitle, all seven
     * fields, and that the Cart's contents (the Pilot Product) were
     * preserved across the Proceed To Checkout transition — read via the
     * shared header's cart badge, which Phase 11.7's UI-hierarchy dumps
     * confirmed is the same {@code cartRL}/{@code cartTV} element regardless
     * of which screen currently hosts it (Login, Cart, or Shipping), not a
     * Catalog-only element.
     */
    private void verifyShippingScreen(CheckoutPage checkoutPage, ProductItem pilotProduct) {
        CommonAssertions.verifyVisible(checkoutPage.isDisplayed(), "Shipping Address screen title");
        CommonAssertions.verifyVisible(checkoutPage.isSubtitleDisplayed(), "Shipping Address screen subtitle");
        CommonAssertions.verifyVisible(checkoutPage.areAllFieldsDisplayed(), "Shipping Address screen — all seven fields");

        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isCartBadgeDisplayed(), "Cart Badge (contents preserved through checkout navigation)");
        CommonAssertions.verifyText(productsPage.getCartBadgeCount().orElse(null), "1",
                "Cart Badge value (Pilot Product '" + pilotProduct.name() + "' still in Cart)");
    }

    /**
     * Reaches the Cart screen with the Pilot Product already added — reuses
     * {@code ProductsPage}/{@code ProductDetailsPage} exactly as
     * {@code ProductDetailsTest.addProductToCart()} (TC-012) does, so this
     * test class doesn't duplicate that navigation logic.
     */
    private CartPage addPilotProductToCartAndOpenCart(ProductItem pilotProduct, String screenshotPrefix) {
        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen");

        String cardName = productsPage.verifyProductCardExists(pilotProduct.name());
        CommonAssertions.verifyText(cardName, pilotProduct.name(), "Product Card — Product Name");

        productsPage.openProductCard(pilotProduct.name());

        ProductDetailsPage productDetailsPage = new ProductDetailsPage();
        CommonAssertions.verifyVisible(productDetailsPage.isDisplayed(), "Product Details screen");
        productDetailsPage.addToCart();

        productsPage.tapCart();

        CartPage cartPage = new CartPage();
        ScreenshotManager.captureScreenshot(screenshotPrefix + "_01_cart_after_navigation");
        return cartPage;
    }

    /**
     * Formats {@code pilotProduct.price()} to match the AUT's on-screen
     * display convention — same format and rationale as
     * {@code ProductDetailsTest#formattedPrice(ProductItem)}, kept local to
     * this class rather than shared, since it's a small, screen-display
     * concern, not a data-model concern (same design choice already made in
     * {@code ProductDetailsTest}).
     */
    private String formattedPrice(ProductItem pilotProduct) {
        return String.format("$ %.2f", pilotProduct.price());
    }

    /**
     * Formats {@code pilotProduct.price() x quantity} to the AUT's on-screen
     * display convention — the same price-times-quantity relationship
     * FR-017/TC-017 already treats as the Cart's total formula (MA-RS-001
     * FR-017 Acceptance Criteria), applied here for a non-1 quantity.
     */
    private String formattedTotal(ProductItem pilotProduct, int quantity) {
        return String.format("$ %.2f", pilotProduct.price().multiply(BigDecimal.valueOf(quantity)));
    }

    /**
     * @return the currently-rendered Product Catalog's product names, in
     * on-screen order — reflects only the RecyclerView items currently
     * rendered ({@code ProductsPage} class Javadoc), which every sort
     * selection resets to the top of the list (Phase 14.1 real-device
     * evidence), not the full scrollable catalog. Used by TC-006 to verify
     * sort results without hardcoding the catalog's contents.
     */
    private List<String> currentProductNames(ProductsPage productsPage) {
        List<String> names = new ArrayList<>();
        int count = productsPage.getProductCount();
        for (int i = 0; i < count; i++) {
            names.add(productsPage.getProductName(i));
        }
        return names;
    }

    /** Same scope as {@link #currentProductNames(ProductsPage)}, parsed from the AUT's {@code "$ 29.99"} price display format into {@link BigDecimal} for numeric ordering comparisons. */
    private List<BigDecimal> currentProductPrices(ProductsPage productsPage) {
        List<BigDecimal> prices = new ArrayList<>();
        int count = productsPage.getProductCount();
        for (int i = 0; i < count; i++) {
            prices.add(new BigDecimal(productsPage.getProductPrice(i).replace("$", "").trim()));
        }
        return prices;
    }

    /**
     * Asserts every adjacent pair in {@code names} is in non-decreasing
     * alphabetical order — a dynamic, pairwise check rather than a
     * hardcoded expected list, since the Product Catalog's exact contents
     * are AUT data (see TC-006's own Javadoc). Individually asserted per
     * adjacent pair so a failure pinpoints exactly which two products are
     * out of order.
     */
    private void verifyNameOrderAscending(List<String> names, String sortLabel) {
        for (int i = 0; i < names.size() - 1; i++) {
            CommonAssertions.verifyVisible(names.get(i).compareTo(names.get(i + 1)) <= 0,
                    sortLabel + " ordering — '" + names.get(i) + "' (position " + i + ") before '" + names.get(i + 1) + "' (position " + (i + 1) + ")");
        }
    }

    /** @see #verifyNameOrderAscending(List, String) */
    private void verifyNameOrderDescending(List<String> names, String sortLabel) {
        for (int i = 0; i < names.size() - 1; i++) {
            CommonAssertions.verifyVisible(names.get(i).compareTo(names.get(i + 1)) >= 0,
                    sortLabel + " ordering — '" + names.get(i) + "' (position " + i + ") before '" + names.get(i + 1) + "' (position " + (i + 1) + ")");
        }
    }

    /** @see #verifyNameOrderAscending(List, String) */
    private void verifyPriceOrderAscending(List<BigDecimal> prices, String sortLabel) {
        for (int i = 0; i < prices.size() - 1; i++) {
            CommonAssertions.verifyVisible(prices.get(i).compareTo(prices.get(i + 1)) <= 0,
                    sortLabel + " ordering — " + prices.get(i) + " (position " + i + ") before " + prices.get(i + 1) + " (position " + (i + 1) + ")");
        }
    }

    /** @see #verifyNameOrderAscending(List, String) */
    private void verifyPriceOrderDescending(List<BigDecimal> prices, String sortLabel) {
        for (int i = 0; i < prices.size() - 1; i++) {
            CommonAssertions.verifyVisible(prices.get(i).compareTo(prices.get(i + 1)) >= 0,
                    sortLabel + " ordering — " + prices.get(i) + " (position " + i + ") before " + prices.get(i + 1) + " (position " + (i + 1) + ")");
        }
    }
}
