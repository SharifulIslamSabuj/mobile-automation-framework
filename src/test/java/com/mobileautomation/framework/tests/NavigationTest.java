package com.mobileautomation.framework.tests;

import com.mobileautomation.framework.assertions.CommonAssertions;
import com.mobileautomation.framework.core.BaseTest;
import com.mobileautomation.framework.pages.NavigationDrawerPage;
import com.mobileautomation.framework.pages.ProductsPage;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import org.testng.annotations.Test;

import java.util.function.BooleanSupplier;

/**
 * Navigation Module (MA-RS-001 §6.10). New test class — Navigation Drawer
 * item destinations don't fit any existing test class's own module scope
 * (LoginTest: Authentication; ProductDetailsTest: Product Details; CartTest:
 * Cart/Checkout, plus TC-006 Product Browsing per Phase 14.1's explicit
 * instruction for that specific phase). Added Phase 15.1 for TC-028.
 */
public class NavigationTest extends BaseTest {

    /**
     * TC-028 — Navigation Drawer Item Access. Verifies every drawer item
     * with real-device-confirmed runtime evidence (Phase 15.1 investigation)
     * individually: displayed in the drawer, tapped, resulting destination
     * confirmed, and — where the destination is a navigable screen — return
     * navigation back to the Product Catalog confirmed. Covers seven
     * destination screens (WebView, QR Code Scanner, Geo Location, Drawing,
     * About, FingerPrint, Virtual USB) plus the Reset App State confirmation
     * dialog. Catalog and Login are not re-verified here — already covered
     * by TC-001/TC-002/TC-003/TC-004/TC-018.
     * <p>
     * <b>Permanently out of scope: Crash app (debug).</b> It intentionally
     * terminates the app process (MA-LOC-001 §14 row 9) — not automated, not
     * tapped, not referenced by any locator or method in this framework.
     * <p>
     * <b>Reset App State</b> is confirmed via its dialog only — Cancel is
     * always tapped, never Confirm/"RESET APP", since actually resetting app
     * state would destructively mutate state for any test running after this
     * one in the same suite.
     * <p>
     * <b>Virtual USB</b> is confirmed real-device (Phase 15.1) to be a
     * separate Android Activity, not a fragment like the other six
     * destinations — it renders with no shared header (no Menu/Sort/Cart
     * icons), consistent with MA-LOC-001 §14 row 8's structural note. The
     * system back gesture ({@code NavigationDrawerPage#navigateBack()}) was
     * confirmed this phase to correctly return from it to the Product
     * Catalog regardless.
     */
    @Test(description = "TC-028 — Navigation Drawer Item Access")
    public void accessDrawerItems() {
        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen");

        NavigationDrawerPage drawerPage = new NavigationDrawerPage();

        verifyDrawerDestination(productsPage, drawerPage, "WebView",
                drawerPage::isWebViewTitleDisplayed, "WebView screen — title", "tc028_01_webview");
        verifyDrawerDestination(productsPage, drawerPage, "QR Code Scanner",
                drawerPage::isQrCodeScannerTitleDisplayed, "QR Code Scanner screen — title", "tc028_02_qr_code_scanner");
        verifyDrawerDestination(productsPage, drawerPage, "Geo Location",
                drawerPage::isGeoLocationTitleDisplayed, "Geo Location screen — title", "tc028_03_geo_location");
        verifyDrawerDestination(productsPage, drawerPage, "Drawing",
                drawerPage::isDrawingTitleDisplayed, "Drawing screen — title", "tc028_04_drawing");
        verifyDrawerDestination(productsPage, drawerPage, "About",
                drawerPage::isAboutTitleDisplayed, "About screen — title", "tc028_05_about");
        // FingerPrint — handled inline, not via verifyDrawerDestination, because this AUT shows a
        // native "Biometrics not supported" AlertDialog on load when the device/emulator has no
        // biometric hardware (confirmed absent on this CI emulator; not seen in this project's
        // real-device evidence). Dismissed only if present — a no-op on hardware where it never
        // appears (Phase 18 investigation).
        logger.info("Selecting drawer item: FingerPrint.");
        productsPage.tapMenu();
        CommonAssertions.verifyVisible(productsPage.isDrawerItemDisplayed("FingerPrint"), "Navigation Drawer — FingerPrint item");
        productsPage.tapDrawerItem("FingerPrint");
        ScreenshotManager.captureScreenshot("tc028_06_fingerprint_destination");
        if (drawerPage.isBiometricsDialogDisplayed()) {
            logger.info("Biometrics dialog present (no biometric hardware on this device/emulator) — dismissing.");
            drawerPage.dismissBiometricsDialog();
        }
        CommonAssertions.verifyVisible(drawerPage.isFingerprintTitleDisplayed(), "FingerPrint screen — title");
        drawerPage.navigateBack();
        ScreenshotManager.captureScreenshot("tc028_06_fingerprint_returned_to_catalog");
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after returning from FingerPrint)");

        verifyDrawerDestination(productsPage, drawerPage, "Virtual USB",
                drawerPage::isVirtualUsbMessageDisplayed, "Virtual USB screen — dynamic status message", "tc028_07_virtual_usb");

        // Reset App State — dialog-only destination (MainActivity.showResetDialog()), not a navigable screen.
        // Expected Result: "Reset App State" responds to activation. Cancel is tapped, never Confirm — see class Javadoc.
        logger.info("Selecting Reset App State.");
        productsPage.tapMenu();
        CommonAssertions.verifyVisible(productsPage.isDrawerItemDisplayed("Reset App State"), "Navigation Drawer — Reset App State item");
        productsPage.tapDrawerItem("Reset App State");
        ScreenshotManager.captureScreenshot("tc028_08_reset_app_state_dialog");
        CommonAssertions.verifyVisible(drawerPage.isResetDialogDisplayed(), "Reset App State dialog — title");
        drawerPage.cancelResetDialog();
        ScreenshotManager.captureScreenshot("tc028_09_reset_app_state_cancelled");
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after cancelling Reset App State)");

        logger.info("TC-028 completed: all seven verified Navigation Drawer destination screens and the Reset App State dialog individually confirmed — Catalog, Login, and Crash app (debug) out of this Test Case's scope.");
    }

    /**
     * Opens the Navigation Drawer, verifies {@code itemText} is displayed,
     * taps it, verifies the resulting destination via {@code isDestinationDisplayed},
     * then navigates back and verifies the Product Catalog screen is
     * displayed again — the uniform flow shared by the seven navigable
     * (non-dialog) drawer destinations this Test Case covers. Reset App
     * State is handled separately (inline in {@link #accessDrawerItems()})
     * since it produces a dialog, not a navigable screen, and must be
     * dismissed via Cancel rather than the system back gesture.
     */
    private void verifyDrawerDestination(ProductsPage productsPage, NavigationDrawerPage drawerPage,
                                          String itemText, BooleanSupplier isDestinationDisplayed,
                                          String destinationElementName, String screenshotPrefix) {
        logger.info("Selecting drawer item: {}.", itemText);
        productsPage.tapMenu();
        CommonAssertions.verifyVisible(productsPage.isDrawerItemDisplayed(itemText), "Navigation Drawer — " + itemText + " item");

        productsPage.tapDrawerItem(itemText);
        ScreenshotManager.captureScreenshot(screenshotPrefix + "_destination");
        CommonAssertions.verifyVisible(isDestinationDisplayed.getAsBoolean(), destinationElementName);

        drawerPage.navigateBack();
        ScreenshotManager.captureScreenshot(screenshotPrefix + "_returned_to_catalog");
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after returning from " + itemText + ")");
    }
}
