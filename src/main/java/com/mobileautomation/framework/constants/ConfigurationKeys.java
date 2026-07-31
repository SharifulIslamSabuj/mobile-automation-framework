package com.mobileautomation.framework.constants;

/**
 * Every configuration property key and system-property name recognized by
 * the Configuration Layer. These are true constants — fixed key names, never
 * configurable values — so that no class outside {@code config} ever types a
 * property-key string literal ("magic string") directly.
 */
public final class ConfigurationKeys {

    private ConfigurationKeys() {
    }

    /** JVM system property used to select the active {@code Environment} (e.g. {@code -Denv=real-device}). */
    public static final String ENVIRONMENT_SYSTEM_PROPERTY = "env";

    // Appium server connection (added Phase 4 — Driver Management needed a server
    // endpoint that Phase 3's property list did not anticipate; see
    // docs/framework/DRIVER_ARCHITECTURE.md for the rationale)
    public static final String APPIUM_SERVER_URL = "appium.serverUrl";

    // Application
    public static final String APP_PATH = "app.path";
    public static final String APP_PACKAGE = "app.package";
    public static final String APP_ACTIVITY = "app.activity";

    // Device
    public static final String DEVICE_NAME = "device.name";
    public static final String PLATFORM_NAME = "platform.name";
    public static final String PLATFORM_VERSION = "platform.version";
    public static final String DEVICE_UDID = "device.udid";
    public static final String AUTOMATION_NAME = "automation.name";

    // Driver
    public static final String DRIVER_NEW_COMMAND_TIMEOUT_SECONDS = "driver.newCommandTimeoutSeconds";
    public static final String DRIVER_IMPLICIT_WAIT_SECONDS = "driver.implicitWaitSeconds";
    public static final String DRIVER_EXPLICIT_WAIT_TIMEOUT_SECONDS = "driver.explicitWaitTimeoutSeconds";
    public static final String DRIVER_PAGE_LOAD_TIMEOUT_SECONDS = "driver.pageLoadTimeoutSeconds";

    // Execution
    public static final String EXECUTION_MODE = "execution.mode";
    public static final String EXECUTION_NO_RESET = "execution.noReset";
    public static final String EXECUTION_FULL_RESET = "execution.fullReset";
    public static final String EXECUTION_AUTO_GRANT_PERMISSIONS = "execution.autoGrantPermissions";
    public static final String EXECUTION_RETRY_COUNT = "execution.retryCount";

    // Reporting
    public static final String REPORT_DIRECTORY = "report.directory";
    public static final String REPORT_SCREENSHOT_DIRECTORY = "report.screenshotDirectory";

    // Logging
    public static final String LOG_LEVEL = "log.level";
    // Added Phase 6 — Cross-Cutting Infrastructure needed a log file directory
    // that Phase 3's property list did not anticipate; see
    // docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md for the rationale.
    public static final String LOGGING_DIRECTORY = "logging.directory";
}
