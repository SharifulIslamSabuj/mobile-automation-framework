package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.driver.DriverProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

import java.time.Duration;
import java.util.List;

/**
 * Generic Android runtime-permission-dialog handling. These are Android
 * <b>operating-system</b> dialogs (package {@code com.android.permissioncontroller}),
 * identical across every app that requests a runtime permission on a given
 * OS/permission-controller version — not this project's AUT. Using their
 * resource-ids here is therefore not "hardcoding an application-specific
 * dialog": no app package name, screen name, or AUT-specific locator from
 * MA-LOC-001 appears anywhere in this class. The specific resource-ids used
 * below were directly confirmed via this project's own Phase 2 device
 * evidence (the Geo Location and Drawing permission-dialog captures).
 * <p>
 * Android shows two dialog shapes depending on the permission type:
 * a 3-button "foreground/one-time/deny" dialog (e.g. Location) and a
 * 2-button "allow/deny" dialog (e.g. Storage, Camera). This class handles
 * both without the caller needing to know which one to expect.
 */
public final class PermissionUtility {

    private static final String PERMISSION_CONTROLLER_PACKAGE = "com.android.permissioncontroller:id/";

    private static final By DIALOG_MESSAGE = By.id(PERMISSION_CONTROLLER_PACKAGE + "permission_message");
    private static final By ALLOW_FOREGROUND_ONLY_BUTTON = By.id(PERMISSION_CONTROLLER_PACKAGE + "permission_allow_foreground_only_button");
    private static final By ALLOW_ONE_TIME_BUTTON = By.id(PERMISSION_CONTROLLER_PACKAGE + "permission_allow_one_time_button");
    private static final By ALLOW_BUTTON = By.id(PERMISSION_CONTROLLER_PACKAGE + "permission_allow_button");
    private static final By DENY_BUTTON = By.id(PERMISSION_CONTROLLER_PACKAGE + "permission_deny_button");

    private PermissionUtility() {
    }

    /** @return {@code true} if a runtime-permission dialog is on screen right now (no wait). */
    public static boolean isPermissionDialogDisplayed() {
        return !DriverProvider.getDriver().findElements(DIALOG_MESSAGE).isEmpty();
    }

    /**
     * Accepts whichever "allow" option is the most persistent one available:
     * the 3-button dialog's "While using the app" if present, otherwise the
     * 2-button dialog's plain "Allow". Prefer {@link #acceptPermissionOnce()}
     * if a one-time grant is specifically required.
     */
    public static void acceptPermission() {
        List<WebElement> foregroundOnly = DriverProvider.getDriver().findElements(ALLOW_FOREGROUND_ONLY_BUTTON);
        if (!foregroundOnly.isEmpty()) {
            foregroundOnly.get(0).click();
            return;
        }
        DriverProvider.getDriver().findElement(ALLOW_BUTTON).click();
    }

    /** Accepts the 3-button dialog's "Only this time" option. */
    public static void acceptPermissionOnce() {
        DriverProvider.getDriver().findElement(ALLOW_ONE_TIME_BUTTON).click();
    }

    /** Denies the current permission dialog (present on both the 2-button and 3-button dialog shapes). */
    public static void denyPermission() {
        DriverProvider.getDriver().findElement(DENY_BUTTON).click();
    }

    /**
     * Waits (explicit wait only) for a permission dialog to appear.
     *
     * @return {@code true} if a dialog appeared within the timeout, {@code false} on timeout (not treated as a failure)
     */
    public static boolean waitForPermissionDialog(Duration timeout) {
        try {
            WaitUtility.waitForPresence(DIALOG_MESSAGE, timeout);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }
}
