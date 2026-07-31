package com.mobileautomation.framework.driver;

import com.mobileautomation.framework.config.CapabilityConfiguration;
import org.openqa.selenium.Capabilities;

/**
 * Extension point for turning a framework-owned {@link CapabilityConfiguration}
 * into a concrete Selenium/Appium {@link Capabilities} implementation.
 * <p>
 * {@link UiAutomator2CapabilityBuilder} is the only implementation today.
 * A future platform or cloud-vendor integration (iOS/XCUITest, BrowserStack,
 * Sauce Labs) adds a new implementation of this interface — {@link DriverFactory}
 * implementations depend on this interface, not on a concrete builder, so no
 * existing class needs to change to support one.
 */
public interface CapabilityBuilder {

    /**
     * Builds the driver-session capabilities from the given configuration.
     *
     * @throws com.mobileautomation.framework.exceptions.DriverInitializationException if a required capability value is invalid
     */
    Capabilities build(CapabilityConfiguration capabilityConfiguration);
}
