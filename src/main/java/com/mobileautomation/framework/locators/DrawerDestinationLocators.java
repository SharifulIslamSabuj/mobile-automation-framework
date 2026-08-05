package com.mobileautomation.framework.locators;

import org.openqa.selenium.By;

/**
 * Drawer Destination Screens (MA-LOC-001 §15/§17) — the screens/dialog
 * reached by tapping a Navigation Drawer item other than Catalog or Login
 * (Catalog is {@code ProductsPage}'s own scope; Login is {@code LoginLocators}).
 * Every value is quoted directly from MA-LOC-001 §15/§17's source-decompiled
 * evidence, corroborated on the real device this phase (Phase 15.1): each
 * title/message element below was confirmed actually displayed after
 * tapping its corresponding drawer item.
 * <p>
 * Only one element per destination is modeled — a title (or, for Virtual
 * USB, its one id-bearing element) — because FR-028's Acceptance Criteria
 * only requires confirming each item "is selectable" (produces a
 * confirmable navigation); the destination screens' own business content
 * beyond that is explicitly Out of Current Observation (MA-RS-001 FR-028),
 * so no deeper locator surface is defined here.
 * <p>
 * <b>Crash app (debug) is permanently out of scope</b> (MA-LOC-001 §14 row
 * 9: launches {@code DebugCrashActivity}, which intentionally terminates
 * the app) — no locator for it exists here or anywhere in this framework.
 */
public final class DrawerDestinationLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By WEBVIEW_TITLE = By.id(PACKAGE + "webViewTV");
    public static final By QR_CODE_SCANNER_TITLE = By.id(PACKAGE + "qrCodeTV");
    public static final By GEO_LOCATION_TITLE = By.id(PACKAGE + "locationTV");
    public static final By DRAWING_TITLE = By.id(PACKAGE + "drawingTV");
    public static final By ABOUT_TITLE = By.id(PACKAGE + "aboutTV");
    public static final By FINGERPRINT_TITLE = By.id(PACKAGE + "bioMetricTV");

    /** Virtual USB is a separate Activity, not a fragment (MA-LOC-001 §14 row 8) — confirmed real-device: no shared header, no other id-bearing element on screen (§15.8). */
    public static final By VIRTUAL_USB_MESSAGE = By.id(PACKAGE + "virtual_usb_message");

    /**
     * Reset App State confirmation — a standard Android platform
     * {@code AlertDialog}, not app-owned XML (MA-LOC-001 §17).
     * <p>
     * <b>Title id corrected Phase 15.1</b>: MA-LOC-001 §17 documents this as
     * {@code android:id/alertTitle} (platform-namespaced), citing "Confirmed
     * matching Phase 2 device evidence" — but a fresh {@code uiautomator dump}
     * on this project's real device (vivo I2301, Android 15), taken after a
     * genuine, reproducible (3/3) automated test failure at this exact
     * locator, showed the title view actually resolves to
     * {@code com.saucelabs.mydemoapp.android:id/alertTitle} — app-namespaced,
     * not platform-namespaced. Message/Cancel/Confirm were independently
     * confirmed in the same dump to remain correctly platform-namespaced
     * ({@code android:id/message}, {@code android:id/button2}, {@code android:id/button1}) —
     * only the title differs. This is a genuine AUT/documentation
     * discrepancy on this specific device, not a test, environment, or
     * framework defect — MA-LOC-001 §17 itself is not modified here
     * (documentation updates are out of this phase's scope), only this
     * framework's own locator.
     */
    public static final By RESET_DIALOG_TITLE = By.id(PACKAGE + "alertTitle");
    public static final By RESET_DIALOG_MESSAGE = By.id("android:id/message");
    public static final By RESET_DIALOG_CANCEL_BUTTON = By.id("android:id/button2");
    public static final By RESET_DIALOG_CONFIRM_BUTTON = By.id("android:id/button1");

    private DrawerDestinationLocators() {
    }
}
