package com.mobileautomation.framework.data.factory;

import com.mobileautomation.framework.data.generator.FakerProvider;
import com.mobileautomation.framework.models.ProductItem;
import net.datafaker.Faker;

import java.math.BigDecimal;

/** Combines Faker-generated catalog data into a ready-to-use {@link ProductItem} — generic, not sourced from any AUT's real catalog. */
public final class ProductDataFactory {

    public ProductDataFactory() {
    }

    /** A fully synthetic catalog item. */
    public ProductItem fakeProduct() {
        Faker faker = FakerProvider.get();
        return new ProductItem(
                faker.commerce().productName(),
                "SKU-" + faker.number().digits(6),
                new BigDecimal(faker.commerce().price(1, 500)),
                faker.commerce().department()
        );
    }
}
