package com.mobileautomation.framework.data.environment;

import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.config.Environment;
import com.mobileautomation.framework.constants.ConfigurationDefaults;

/**
 * Resolves a relative test-data resource path (e.g. {@code "login/credentials.json"})
 * against the active {@link Environment} first —
 * {@code testdata/<profileName>/login/credentials.json} — falling back to
 * the environment-agnostic common folder,
 * {@code testdata/common/login/credentials.json}, if no environment-specific
 * override exists on the classpath. Configuration-driven: the environment
 * segment always comes from {@link ConfigReader#getEnvironment()}, never a
 * hardcoded environment name.
 */
public final class TestDataEnvironmentResolver {

    private TestDataEnvironmentResolver() {
    }

    public static String resolve(String relativeResourcePath) {
        Environment environment = ConfigReader.getInstance().getEnvironment();
        String environmentSpecificPath = ConfigurationDefaults.TESTDATA_RESOURCE_DIRECTORY
                + environment.getProfileName() + "/" + relativeResourcePath;
        if (resourceExists(environmentSpecificPath)) {
            return environmentSpecificPath;
        }
        return ConfigurationDefaults.TESTDATA_RESOURCE_DIRECTORY
                + ConfigurationDefaults.TESTDATA_COMMON_FOLDER + relativeResourcePath;
    }

    private static boolean resourceExists(String resourcePath) {
        return TestDataEnvironmentResolver.class.getClassLoader().getResource(resourcePath) != null;
    }
}
