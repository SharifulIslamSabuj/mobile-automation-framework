package com.mobileautomation.framework.locators;

import io.appium.java_client.AppiumBy;
import org.openqa.selenium.By;

/**
 * Login screen locators. Every value is quoted directly from MA-LOC-001 §5
 * (Locator Repository) — none is invented, none is duplicated elsewhere.
 */
public final class LoginLocators {

    private static final String PACKAGE = "com.saucelabs.mydemoapp.android:id/";

    public static final By LOGIN_TITLE = By.id(PACKAGE + "loginTV");
    public static final By USERNAME_FIELD = By.id(PACKAGE + "nameET");
    public static final By USERNAME_ERROR_LABEL = By.id(PACKAGE + "nameErrorTV");
    public static final By PASSWORD_FIELD = By.id(PACKAGE + "passwordET");
    public static final By PASSWORD_ERROR_LABEL = By.id(PACKAGE + "passwordErrorTV");
    public static final By LOGIN_BUTTON = AppiumBy.accessibilityId("Tap to login with given credentials");

    // Saved credentials section (default demo data — row 1/3 are usable; row 2 is source-confirmed locked out, MA-LOC-001 §18).
    public static final By SAVED_CREDENTIAL_ROW_1_USERNAME = By.id(PACKAGE + "username1TV");
    public static final By SAVED_CREDENTIAL_ROW_1_PASSWORD = By.id(PACKAGE + "password1TV");
    public static final By SAVED_CREDENTIAL_ROW_2_USERNAME = By.id(PACKAGE + "username2TV");
    public static final By SAVED_CREDENTIAL_ROW_2_PASSWORD = By.id(PACKAGE + "password2TV");
    public static final By SAVED_CREDENTIAL_ROW_3_USERNAME = By.id(PACKAGE + "username3TV");
    public static final By SAVED_CREDENTIAL_ROW_3_PASSWORD = By.id(PACKAGE + "password3TV");

    private LoginLocators() {
    }
}
