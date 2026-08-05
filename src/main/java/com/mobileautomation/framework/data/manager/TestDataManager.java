package com.mobileautomation.framework.data.manager;

import com.mobileautomation.framework.data.environment.TestDataEnvironmentResolver;
import com.mobileautomation.framework.data.factory.LoginDataFactory;
import com.mobileautomation.framework.data.factory.PaymentDataFactory;
import com.mobileautomation.framework.data.factory.ProductDataFactory;
import com.mobileautomation.framework.data.factory.ShippingDataFactory;
import com.mobileautomation.framework.data.factory.UserDataFactory;
import com.mobileautomation.framework.data.loader.DataLoader;

/**
 * The single entry point every future test uses for test data — hides
 * {@code data.reader}/{@code data.loader} entirely (future tests must never
 * reference a reader or {@link DataLoader} directly; only the compiler
 * doesn't enforce this the way {@code DriverProvider}/{@code ReportProvider}
 * enforce their package-private managers, since this phase's requested
 * package structure spreads reader/loader/manager across separate
 * sub-packages — see the Architectural Decisions section of
 * docs/framework/TEST_DATA_FRAMEWORK.md).
 * <p>
 * Thread-safe double-checked-locking singleton, matching
 * {@code ConfigReader}/{@code ExtentReportManager}'s established pattern.
 * Owns one {@link DataLoader} instance for the JVM's lifetime, which is what
 * makes its dataset cache meaningful across every caller.
 */
public final class TestDataManager {

    private static volatile TestDataManager instance;

    private final DataLoader dataLoader = new DataLoader();

    private TestDataManager() {
    }

    public static TestDataManager getInstance() {
        TestDataManager result = instance;
        if (result == null) {
            synchronized (TestDataManager.class) {
                result = instance;
                if (result == null) {
                    instance = result = new TestDataManager();
                }
            }
        }
        return result;
    }

    /** Loads {@code resourcePath} exactly as given (no environment resolution) — for a resource that is intentionally environment-agnostic. */
    public <T> T loadData(String resourcePath, Class<T> targetType) {
        return dataLoader.load(resourcePath, targetType);
    }

    /** Loads {@code relativeResourcePath} resolved against the active Environment first, falling back to the common dataset — see {@link TestDataEnvironmentResolver}. */
    public <T> T loadEnvironmentData(String relativeResourcePath, Class<T> targetType) {
        return dataLoader.load(TestDataEnvironmentResolver.resolve(relativeResourcePath), targetType);
    }

    public LoginDataFactory loginData() {
        return new LoginDataFactory(this);
    }

    public UserDataFactory userData() {
        return new UserDataFactory();
    }

    public ProductDataFactory productData() {
        return new ProductDataFactory(this);
    }

    public ShippingDataFactory shippingData() {
        return new ShippingDataFactory(this);
    }

    public PaymentDataFactory paymentData() {
        return new PaymentDataFactory(this);
    }
}
