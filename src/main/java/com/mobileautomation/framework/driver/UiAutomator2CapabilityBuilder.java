package com.mobileautomation.framework.driver;

import com.mobileautomation.framework.config.CapabilityConfiguration;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.Capabilities;

import java.time.Duration;

/**
 * Builds Appium {@code UiAutomator2Options} capabilities from a
 * {@link CapabilityConfiguration}. No capability value is hardcoded here —
 * every value is read from the configuration model passed in. Blank/absent
 * optional values (e.g. {@code udid}, {@code platformVersion} on an
 * emulator) are simply omitted from the built capabilities rather than sent
 * as empty strings, so Appium falls back to its own defaults for them.
 */
public class UiAutomator2CapabilityBuilder implements CapabilityBuilder {

    @Override
    public Capabilities build(CapabilityConfiguration capabilityConfiguration) {
        UiAutomator2Options options = new UiAutomator2Options();

        options.setPlatformName(capabilityConfiguration.getPlatformName());

        if (isPresent(capabilityConfiguration.getPlatformVersion())) {
            options.setPlatformVersion(capabilityConfiguration.getPlatformVersion());
        }
        if (isPresent(capabilityConfiguration.getDeviceName())) {
            options.setDeviceName(capabilityConfiguration.getDeviceName());
        }
        if (isPresent(capabilityConfiguration.getUdid())) {
            options.setUdid(capabilityConfiguration.getUdid());
        }
        if (isPresent(capabilityConfiguration.getAppPackage())) {
            options.setAppPackage(capabilityConfiguration.getAppPackage());
        }
        if (isPresent(capabilityConfiguration.getAppActivity())) {
            options.setAppActivity(capabilityConfiguration.getAppActivity());
        }
        if (isPresent(capabilityConfiguration.getAppPath())) {
            options.setApp(capabilityConfiguration.getAppPath());
        }

        options.setNewCommandTimeout(Duration.ofSeconds(capabilityConfiguration.getNewCommandTimeoutSeconds()));
        options.setNoReset(capabilityConfiguration.isNoReset());
        options.setFullReset(capabilityConfiguration.isFullReset());
        options.setAutoGrantPermissions(capabilityConfiguration.isAutoGrantPermissions());

        return options;
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
