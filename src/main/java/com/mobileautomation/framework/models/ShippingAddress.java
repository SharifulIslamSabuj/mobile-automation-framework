package com.mobileautomation.framework.models;

import com.mobileautomation.framework.data.validator.Required;

import java.io.Serializable;

/**
 * Shipping Address dataset shape (MA-TDD-001 §8.2, TD-002). Required-field
 * marking follows MA-LOC-001 §10's source-confirmed field-requiredness
 * evidence, not invention: Full Name, Address Line 1, City, Zip Code, and
 * Country each have a live {@code TextWatcher} validating them in
 * {@code CheckoutInfoFragment.java}; Address Line 2 and State/Region do not
 * (confirmed: no watcher attached, read only if non-empty in {@code validate()}).
 */
public record ShippingAddress(
        @Required String fullName,
        @Required String addressLine1,
        String addressLine2,
        @Required String city,
        String state,
        @Required String zipCode,
        @Required String country
) implements Serializable {
}
