package com.mobileautomation.framework.pages;

import com.mobileautomation.framework.core.BasePage;
import com.mobileautomation.framework.exceptions.ElementActionException;
import com.mobileautomation.framework.locators.LoginLocators;
import org.openqa.selenium.By;

/**
 * Page Object for the Login screen (MA-LOC-001 §5).
 * <p>
 * <b>Purpose:</b> encapsulate the Login screen's fields, button, and saved
 * (default demo) credential rows behind user-facing actions — nothing else
 * on this screen is in scope for the current pilot.
 * <p>
 * <b>Dependencies:</b> {@link BasePage} (inherited — {@code elementActions}
 * for every interaction), {@link LoginLocators} (the only locator source).
 * <p>
 * <b>Responsibilities:</b> enter credentials, submit the form, read
 * field/error state, tap a saved-credential row.
 * <p>
 * <b>Limitations:</b> the biometric-login button is deliberately not
 * modeled — MA-LOC-001 §5 confirms it is {@code visibility="gone"} unless
 * biometric login is enabled and warns callers not to assume it is present.
 * Tapping a saved-credential row only performs the tap; whether it
 * auto-fills the Username/Password fields is not confirmed anywhere in
 * MA-LOC-001 or MA-AA-001, so no such effect is asserted here — that
 * remains a Test Class's concern to observe and verify.
 */
public class LoginPage extends BasePage {

    /** Clears and re-enters the Username field. */
    public void enterUsername(String username) {
        elementActions.clear(LoginLocators.USERNAME_FIELD);
        elementActions.type(LoginLocators.USERNAME_FIELD, username);
    }

    /** Clears and re-enters the Password field. */
    public void enterPassword(String password) {
        elementActions.clear(LoginLocators.PASSWORD_FIELD);
        elementActions.type(LoginLocators.PASSWORD_FIELD, password);
    }

    /** Taps the Login button. */
    public void tapLogin() {
        elementActions.click(LoginLocators.LOGIN_BUTTON);
    }

    /** Enters both fields and submits the form — composes {@link #enterUsername}, {@link #enterPassword}, {@link #tapLogin}. */
    public void login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        tapLogin();
    }

    /** @return the Username field's current contents. */
    public String getUsernameFieldValue() {
        return elementActions.getText(LoginLocators.USERNAME_FIELD);
    }

    /** @return whether the Username validation error is currently displayed. */
    public boolean isUsernameErrorDisplayed() {
        return elementActions.isDisplayed(LoginLocators.USERNAME_ERROR_LABEL);
    }

    /** @return the Username validation error's text. */
    public String getUsernameErrorText() {
        return elementActions.getText(LoginLocators.USERNAME_ERROR_LABEL);
    }

    /** @return whether the Password validation error is currently displayed. */
    public boolean isPasswordErrorDisplayed() {
        return elementActions.isDisplayed(LoginLocators.PASSWORD_ERROR_LABEL);
    }

    /** @return the Password validation error's text (its content is set dynamically — see MA-LOC-001 §18). */
    public String getPasswordErrorText() {
        return elementActions.getText(LoginLocators.PASSWORD_ERROR_LABEL);
    }

    /**
     * Taps the username cell of the given saved-credential row (the
     * default demo data's rows 1–3, MA-LOC-001 §5). Performs the tap only —
     * see class Javadoc for why no fill effect is asserted.
     *
     * @param rowNumber 1, 2, or 3
     * @throws ElementActionException if {@code rowNumber} is not 1, 2, or 3
     */
    public void tapSavedCredentialRow(int rowNumber) {
        elementActions.click(savedCredentialUsernameLocatorForRow(rowNumber));
    }

    /** @return whether the Login screen's title is displayed. */
    public boolean isDisplayed() {
        return elementActions.isDisplayed(LoginLocators.LOGIN_TITLE);
    }

    private By savedCredentialUsernameLocatorForRow(int rowNumber) {
        switch (rowNumber) {
            case 1:
                return LoginLocators.SAVED_CREDENTIAL_ROW_1_USERNAME;
            case 2:
                return LoginLocators.SAVED_CREDENTIAL_ROW_2_USERNAME;
            case 3:
                return LoginLocators.SAVED_CREDENTIAL_ROW_3_USERNAME;
            default:
                throw new ElementActionException("No saved-credential row locator registered for row: " + rowNumber
                        + ". Valid rows are 1, 2, or 3.");
        }
    }
}
