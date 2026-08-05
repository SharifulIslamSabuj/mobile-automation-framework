package com.mobileautomation.framework.data.factory;

import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.models.ShippingAddress;

/** Environment-aware Shipping Address dataset (MA-TDD-001 §8.2, TD-002) behind one API — mirrors {@link ProductDataFactory}/{@link LoginDataFactory}'s established shape. No business logic. */
public final class ShippingDataFactory {

    private static final String ADDRESS_RESOURCE = "shipping/address.json";

    private final TestDataManager testDataManager;

    public ShippingDataFactory(TestDataManager testDataManager) {
        this.testDataManager = testDataManager;
    }

    /** The canned, environment-aware Shipping Address dataset (TD-002, Ready — MA-TDD-001 §8.2), falling back to the common dataset. */
    public ShippingAddress standardAddress() {
        return testDataManager.loadEnvironmentData(ADDRESS_RESOURCE, ShippingAddress.class);
    }
}
