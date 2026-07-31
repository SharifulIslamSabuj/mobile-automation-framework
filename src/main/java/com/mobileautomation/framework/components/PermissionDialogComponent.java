package com.mobileautomation.framework.components;

import com.mobileautomation.framework.utils.PermissionUtility;

import java.time.Duration;

/**
 * Dialog Component wrapper around {@link PermissionUtility} (Phase 5) —
 * gives the Android OS runtime-permission dialog the same Dialog Component
 * shape as {@link ConfirmationDialogComponent}/{@link AlertDialogComponent},
 * without duplicating its already evidence-verified detection/interaction
 * logic. Does not extend {@link BaseComponent}: the OS dialog's locators
 * are fixed (not caller-supplied — see {@link PermissionUtility}'s own
 * Javadoc for why that is not an AUT-specific hardcoding), so there is no
 * root locator for a caller to provide; composition over inheritance is
 * used here deliberately.
 */
public class PermissionDialogComponent {

    public boolean isDisplayed() {
        return PermissionUtility.isPermissionDialogDisplayed();
    }

    public boolean waitForDisplay(Duration timeout) {
        return PermissionUtility.waitForPermissionDialog(timeout);
    }

    public void accept() {
        PermissionUtility.acceptPermission();
    }

    public void acceptOnce() {
        PermissionUtility.acceptPermissionOnce();
    }

    public void deny() {
        PermissionUtility.denyPermission();
    }
}
