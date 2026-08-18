package com.mobileautomation.framework.tests;

import com.mobileautomation.framework.assertions.CommonAssertions;
import com.mobileautomation.framework.core.BaseTest;
import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.models.LoginCredentials;
import com.mobileautomation.framework.pages.LoginPage;
import com.mobileautomation.framework.pages.ProductsPage;
import com.mobileautomation.framework.reporting.ScreenshotManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * TC-004 — Login Outcome Verification.
 * <p>
 * Reaching the Login screen requires the full precondition chain MA-TC-001
 * documents (TC-004 ← TC-003 ← TC-002 ← TC-008): app launch lands on the
 * Product Catalog screen — confirmed by TC-001 — not on Login directly;
 * Login is reached only via the Navigation Drawer's "Login" item. This
 * class follows that documented chain rather than a shorthand
 * "launch straight to Login" flow.
 * <p>
 * The post-login destination screen is, as of this implementation, still
 * "Out of Current Observation" in MA-TC-001 / MA-AA-001 / MA-RS-001 FR-004
 * (see MA-PAD-001 §16–§17). This test asserts the Pilot Design's
 * evidence-based hypothesis — that login returns the user to the Product
 * Catalog screen — because that is the only screen among this phase's
 * three Page Objects consistent with the app's own confirmed structure (no
 * separate post-login screen is in scope, and the Drawer's "Login" item is
 * documented to toggle to "Logout" once authenticated, MA-LOC-001 §14). If
 * live execution shows otherwise, this assertion fails with evidence — per
 * MA-TC-001 §4's own rule for a "Should"-priority, currently-Blocked test
 * case: document what actually happens, do not invent it.
 * <p>
 * <b>Phase 9.5G evidence enhancement:</b> returning to the Product Catalog
 * alone is not sufficient evidence of successful authentication, since the
 * Catalog is reachable both logged-in and logged-out. This test additionally
 * reopens the Navigation Drawer post-login and asserts the unique
 * authenticated business state — "Log Out" displayed, "Log In" absent — as
 * the primary evidence of successful login, with Catalog re-display kept as
 * a secondary navigation check. Login logic, the framework, and Page Object
 * designs are unchanged; only this additional business verification was added.
 */
@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseTest {

    @Test(description = "TC-004 — Login Outcome Verification")
    @Story("Valid Login")
    @Severity(SeverityLevel.CRITICAL)
    public void loginOutcomeVerification() {
        ProductsPage productsPage = new ProductsPage();
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (post-launch)");

        productsPage.tapMenu();
        ScreenshotManager.captureScreenshot("tc004_01_drawer_before_login_log_in");
        productsPage.tapDrawerItem("Log In");

        LoginPage loginPage = new LoginPage();
        CommonAssertions.verifyVisible(loginPage.isDisplayed(), "Login screen");

        LoginCredentials credentials = TestDataManager.getInstance().loginData().standardCredentials();
        loginPage.login(credentials.username(), credentials.password());

        // Assertion Order #1 — Product Catalog displayed (post-login navigation check).
        CommonAssertions.verifyVisible(productsPage.isDisplayed(),
                "Product Catalog screen (post-login destination — Pilot Design hypothesis, MA-PAD-001 §16)");

        // Assertion Order #2 — reopen the Navigation Drawer to read the authenticated business state.
        logger.info("Opening Navigation Drawer to verify authentication state.");
        productsPage.tapMenu();

        // Assertion Order #3 — "Log Out" exists (primary business assertion for successful authentication).
        logger.info("Verifying Login State: expecting drawer item 'Log Out' to be displayed.");
        boolean logOutDisplayed = productsPage.isDrawerItemDisplayed("Log Out");
        logger.info("Expected Drawer Item: 'Log Out' displayed. Actual: {}.", logOutDisplayed);
        ScreenshotManager.captureScreenshot("tc004_02_drawer_after_login_log_out");
        CommonAssertions.verifyVisible(logOutDisplayed, "Navigation Drawer — 'Log Out' item (authenticated state)");

        // Assertion Order #4 — "Log In" must no longer be present (same drawer state as #3, no new screenshot needed).
        logger.info("Verifying Login State: expecting drawer item 'Log In' to be absent.");
        boolean logInDisplayed = productsPage.isDrawerItemDisplayed("Log In");
        logger.info("Expected Drawer Item: 'Log In' absent. Actual displayed: {}.", logInDisplayed);
        CommonAssertions.verifyHidden(logInDisplayed, "Navigation Drawer — 'Log In' item (must be absent post-authentication)");

        // Assertion Order #5 — Product Catalog remains displayed (secondary navigation check).
        CommonAssertions.verifyVisible(productsPage.isDisplayed(), "Product Catalog screen (after Navigation Drawer verification)");
        ScreenshotManager.captureScreenshot("tc004_03_catalog_after_verification");
    }
}
