package com.mobileautomation.framework.driver;

import com.mobileautomation.framework.config.CapabilityConfiguration;
import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.exceptions.DriverInitializationException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Capabilities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Creates Android/UiAutomator2 {@link AppiumDriver} sessions.
 * <p>
 * The only implementation of {@link DriverFactory} today, matching the
 * frozen stack (MA-PV-001 §16: Android + UiAutomator2). Consumes the
 * Configuration Layer ({@link ConfigReader}, {@link CapabilityConfiguration})
 * exclusively — it never reads a properties file or a system property
 * directly, and it contains no business logic.
 */
public class AndroidDriverFactory implements DriverFactory {

    private final CapabilityBuilder capabilityBuilder;

    public AndroidDriverFactory() {
        this(new UiAutomator2CapabilityBuilder());
    }

    public AndroidDriverFactory(CapabilityBuilder capabilityBuilder) {
        this.capabilityBuilder = capabilityBuilder;
    }

    @Override
    public AppiumDriver createDriver() {
        ConfigReader configReader = ConfigReader.getInstance();
        CapabilityConfiguration capabilityConfiguration = CapabilityConfiguration.fromConfigReader(configReader);
        Capabilities capabilities = capabilityBuilder.build(capabilityConfiguration);
        URL serverUrl = resolveServerUrl(configReader.getAppiumServerUrl());

        try {
            return new AndroidDriver(serverUrl, capabilities);
        } catch (RuntimeException e) {
            throw new DriverInitializationException(
                    "Failed to create Android driver session against Appium server " + serverUrl
                            + ". Capabilities requested: " + capabilities, e);
        }
    }

    private URL resolveServerUrl(String rawUrl) {
        try {
            return URI.create(rawUrl).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new DriverInitializationException(
                    "Invalid Appium server URL in configuration ('" + com.mobileautomation.framework.constants.ConfigurationKeys.APPIUM_SERVER_URL
                            + "'): '" + rawUrl + "'", e);
        }
    }
}
