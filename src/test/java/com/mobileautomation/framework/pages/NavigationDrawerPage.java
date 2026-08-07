package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.locators.DrawerDestinationLocators;

/**
 * Page Object for the screens/dialog reached from the Navigation Drawer
 * (MA-LOC-001 §15/§17), other than Catalog and Login — those are
 * {@code ProductsPage}/{@code LoginPage}'s own scope. Added Phase 15.1
 * (TC-028).
 * <p>
 * <b>Dependencies:</b> drawer opening, item tap, and item-displayed checks
 * reuse {@code ProductsPage#tapMenu()}/{@code #tapDrawerItem(String)}/
 * {@code #isDrawerItemDisplayed(String)} unchanged — this Page Object only
 * models what appears <em>after</em> a drawer item is tapped.
 * <p>
 * <b>Scope:</b> one confirmation element per destination (a title, or for
 * Virtual USB its one id-bearing element), matching FR-028's Acceptance
 * Criteria — "each drawer item is selectable" — not a full business-content
 * verification of each destination screen, which remains explicitly Out of
 * Current Observation (MA-RS-001 FR-028). Crash app (debug) is permanently
 * out of scope — no method here launches or references it.
 */
public class NavigationDrawerPage extends BasePage {

    /** @return whether the WebView (URL Entry) screen's title is displayed. */
    public boolean isWebViewTitleDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.WEBVIEW_TITLE);
    }

    /** @return whether the QR Code Scanner screen's title is displayed. */
    public boolean isQrCodeScannerTitleDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.QR_CODE_SCANNER_TITLE);
    }

    /** @return whether the Geo Location screen's title is displayed. */
    public boolean isGeoLocationTitleDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.GEO_LOCATION_TITLE);
    }

    /** @return whether the Drawing screen's title is displayed. */
    public boolean isDrawingTitleDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.DRAWING_TITLE);
    }

    /** @return whether the About screen's title is displayed. */
    public boolean isAboutTitleDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.ABOUT_TITLE);
    }

    /** @return whether the FingerPrint (Biometrics) screen's title is displayed. */
    public boolean isFingerprintTitleDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.FINGERPRINT_TITLE);
    }

    /** @return whether the Virtual USB Activity's dynamic status message element is displayed — confirmed real-device (Phase 15.1) to have no shared header (a separate Activity, not a fragment, MA-LOC-001 §14 row 8). */
    public boolean isVirtualUsbMessageDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.VIRTUAL_USB_MESSAGE);
    }

    /** @return whether the Reset App State confirmation dialog (a standard Android platform {@code AlertDialog}, MA-LOC-001 §17) is displayed. */
    public boolean isResetDialogDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.RESET_DIALOG_TITLE);
    }

    /** Dismisses the Reset App State dialog via Cancel — never taps Confirm, since that would destructively mutate app state mid-suite. */
    public void cancelResetDialog() {
        elementActions.click(DrawerDestinationLocators.RESET_DIALOG_CANCEL_BUTTON);
    }

    /** @return whether the Biometrics "not supported" dialog (a standard Android platform {@code AlertDialog}) is currently displayed — appears when the FingerPrint screen loads on a device/emulator without biometric hardware. Added Phase 18. */
    public boolean isBiometricsDialogDisplayed() {
        return elementActions.isDisplayed(DrawerDestinationLocators.BIOMETRICS_DIALOG_OK_BUTTON);
    }

    /** Dismisses the Biometrics "not supported" dialog via its OK button. Added Phase 18. */
    public void dismissBiometricsDialog() {
        elementActions.click(DrawerDestinationLocators.BIOMETRICS_DIALOG_OK_BUTTON);
    }

    /** Navigates back via the system back gesture — same mechanism {@code ProductDetailsPage#navigateBack()} already uses, confirmed correct by TC-029; also confirmed this phase to correctly return from Virtual USB's separate Activity, not just a fragment. */
    public void navigateBack() {
        navigationHelper.back();
    }
}
