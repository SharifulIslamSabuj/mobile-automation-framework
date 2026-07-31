package com.mobileautomation.framework.core;

import com.mobileautomation.framework.driver.DriverProvider;
import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.utils.KeyboardUtility;
import io.appium.java_client.android.HasNotifications;
import org.slf4j.Logger;

/**
 * Reusable, generic navigation operations — no application-specific screen
 * navigation (moving between AUT screens is a Page Object concern). Every
 * method obtains the driver fresh from {@link DriverProvider} on each call;
 * this class holds no fields and is stateless/thread-safe.
 */
public class NavigationHelper {

    private static final Logger LOGGER = LogManager.getLogger(NavigationHelper.class);

    public void back() {
        DriverProvider.getDriver().navigate().back();
        LOGGER.info("Navigated back.");
    }

    public void refresh() {
        DriverProvider.getDriver().navigate().refresh();
        LOGGER.info("Refreshed current screen.");
    }

    /** Opens the Android notification shade. */
    public void openNotifications() {
        ((HasNotifications) DriverProvider.getDriver()).openNotifications();
        LOGGER.info("Opened notification shade.");
    }

    /** Hides the soft keyboard only if it is currently shown — safe to call unconditionally. */
    public void closeKeyboardIfShown() {
        if (KeyboardUtility.isKeyboardShown()) {
            KeyboardUtility.hideKeyboard();
            LOGGER.info("Keyboard was shown; hidden.");
        }
    }
}
