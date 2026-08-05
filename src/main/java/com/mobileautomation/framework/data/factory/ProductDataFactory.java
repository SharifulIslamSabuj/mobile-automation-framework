package com.mobileautomation.framework.data.factory;

import com.mobileautomation.framework.data.generator.FakerProvider;
import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.models.ProductItem;
import net.datafaker.Faker;

import java.math.BigDecimal;

/** Combines Faker-generated and environment-aware static catalog data into a ready-to-use {@link ProductItem}. */
public final class ProductDataFactory {

    private static final String PILOT_PRODUCT_RESOURCE = "product/pilot.json";

    private final TestDataManager testDataManager;

    public ProductDataFactory(TestDataManager testDataManager) {
        this.testDataManager = testDataManager;
    }

    /** A fully synthetic catalog item — generic, not sourced from any AUT's real catalog. */
    public ProductItem fakeProduct() {
        Faker faker = FakerProvider.get();
        return new ProductItem(
                faker.commerce().productName(),
                "SKU-" + faker.number().digits(6),
                new BigDecimal(faker.commerce().price(1, 500)),
                faker.commerce().department()
        );
    }

    /** The verified, real product used by the Pilot (TC-012) — environment-aware (via {@code testdata/&lt;environment&gt;/product/pilot.json}, falling back to the common dataset). */
    public ProductItem pilotProduct() {
        return testDataManager.loadEnvironmentData(PILOT_PRODUCT_RESOURCE, ProductItem.class);
    }
}
