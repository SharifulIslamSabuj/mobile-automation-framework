package com.mobileautomation.framework.config;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Immutable, framework-owned snapshot of the Appium desired-capability
 * values sourced from the Configuration Layer.
 * <p>
 * This is a plain data holder — it has no dependency on any Appium/Selenium
 * type. A future Driver layer maps this object onto the driver library's own
 * capability/options type, keeping driver creation independent of how
 * configuration is loaded or parsed. No Appium session is created here and
 * no driver classes are referenced, per the Configuration Layer's scope
 * boundary.
 */
@Getter
@Builder
@ToString
public final class CapabilityConfiguration {

    private final String platformName;
    private final String platformVersion;
    private final String deviceName;
    private final String udid;
    private final String automationName;
    private final String appPackage;
    private final String appActivity;
    private final String appPath;
    private final long newCommandTimeoutSeconds;
    private final boolean noReset;
    private final boolean fullReset;
    private final boolean autoGrantPermissions;

    /**
     * Builds a {@link CapabilityConfiguration} from the current state of the
     * given {@link ConfigReader}.
     */
    public static CapabilityConfiguration fromConfigReader(ConfigReader configReader) {
        return CapabilityConfiguration.builder()
                .platformName(configReader.getPlatformName())
                .platformVersion(configReader.getPlatformVersion())
                .deviceName(configReader.getDeviceName())
                .udid(configReader.getUdid())
                .automationName(configReader.getAutomationName())
                .appPackage(configReader.getAppPackage())
                .appActivity(configReader.getAppActivity())
                .appPath(configReader.getAppPath())
                .newCommandTimeoutSeconds(configReader.getNewCommandTimeout().getSeconds())
                .noReset(configReader.isNoReset())
                .fullReset(configReader.isFullReset())
                .autoGrantPermissions(configReader.isAutoGrantPermissions())
                .build();
    }
}
