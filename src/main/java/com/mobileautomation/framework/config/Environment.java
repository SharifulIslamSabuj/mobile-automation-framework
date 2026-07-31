package com.mobileautomation.framework.config;

import com.mobileautomation.framework.exceptions.ConfigurationException;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The set of supported execution environments. The active environment
 * determines which environment-specific properties file
 * ({@code config-<profileName>.properties}) is layered on top of the common
 * {@code config.properties} file. Selected via the {@code env} system
 * property (see {@link com.mobileautomation.framework.constants.ConfigurationKeys#ENVIRONMENT_SYSTEM_PROPERTY}) —
 * never hardcoded in source.
 */
public enum Environment {

    EMULATOR("emulator"),
    REAL_DEVICE("real-device");

    private final String profileName;

    Environment(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return profileName;
    }

    /**
     * Resolves a profile name (as supplied via the {@code env} system
     * property) to its {@link Environment}.
     *
     * @throws ConfigurationException if the profile name does not match any supported environment
     */
    public static Environment fromProfileName(String profileName) {
        return Arrays.stream(values())
                .filter(env -> env.profileName.equalsIgnoreCase(profileName))
                .findFirst()
                .orElseThrow(() -> new ConfigurationException(
                        "Unsupported environment profile: '" + profileName + "'. Supported profiles: "
                                + Arrays.stream(values())
                                        .map(Environment::getProfileName)
                                        .collect(Collectors.joining(", "))));
    }
}
