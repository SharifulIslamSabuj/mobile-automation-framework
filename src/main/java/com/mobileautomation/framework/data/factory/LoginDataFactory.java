package com.mobileautomation.framework.data.factory;

import com.mobileautomation.framework.data.generator.RandomDataGenerator;
import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.data.provider.NegativeDataProvider;
import com.mobileautomation.framework.models.LoginCredentials;

/**
 * Combines static (environment-aware JSON) and random credential data
 * behind one API — a future Page Object/Test Class never needs to know
 * whether a given {@link LoginCredentials} came from a file or was
 * generated. No business logic: this class does not know what "successful
 * login" means, only how to produce a credentials object.
 */
public final class LoginDataFactory {

    private static final String CREDENTIALS_RESOURCE = "login/credentials.json";

    private final TestDataManager testDataManager;

    public LoginDataFactory(TestDataManager testDataManager) {
        this.testDataManager = testDataManager;
    }

    /** The canned, environment-aware credential set (e.g. {@code testdata/<environment>/login/credentials.json}, falling back to the common dataset). */
    public LoginCredentials standardCredentials() {
        return testDataManager.loadEnvironmentData(CREDENTIALS_RESOURCE, LoginCredentials.class);
    }

    /** A freshly-generated, random credential pair — for scenarios that must not depend on a fixed dataset. */
    public LoginCredentials randomCredentials() {
        return new LoginCredentials(RandomDataGenerator.randomUsername(), RandomDataGenerator.randomString(12));
    }

    /** Both fields blank — a reusable negative-data credential pair. */
    public LoginCredentials blankCredentials() {
        return new LoginCredentials(NegativeDataProvider.emptyString(), NegativeDataProvider.emptyString());
    }
}
