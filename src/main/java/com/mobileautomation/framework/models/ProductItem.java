package com.mobileautomation.framework.models;

import com.mobileautomation.framework.data.validator.Required;

import java.io.Serializable;
import java.math.BigDecimal;

/** Generic catalog-item shape (name/sku/price/category). No application-specific meaning — not sourced from any AUT-specific product data. */
public record ProductItem(
        @Required String name,
        String sku,
        BigDecimal price,
        String category
) implements Serializable {
}
