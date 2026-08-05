package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Navigation Drawer item locator, quoted directly from MA-LOC-001 §14 — not
 * a Page Object (no {@code NavigationDrawerPage} exists; Navigation Drawer
 * pages are explicitly out of the current phase's scope). {@code itemTV}
 * repeats identically across all 11 drawer rows and most rows have no
 * unique accessibility id (MA-LOC-001 §14), so the row's visible text is
 * the only reliable differentiator — a documented Priority-3 compound
 * locator (resource-id + text), not an invented strategy.
 */
public final class NavigationDrawerLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    private NavigationDrawerLocators() {
    }

    /** @param itemText the drawer row's exact visible text, e.g. {@code "Login"} (MA-LOC-001 §14 row 10, logged-out state). */
    public static By drawerItem(String itemText) {
        return AppiumBy.androidUIAutomator(
                "new UiSelector().resourceId(\"" + PACKAGE + "itemTV\").text(\"" + itemText + "\")");
    }
}
