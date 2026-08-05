package com.mobileautomation.framework.models;

import com.mobileautomation.framework.data.validator.Required;

import java.io.Serializable;

/**
 * Payment Card dataset shape (MA-TDD-001 §8.3, TD-003). All four fields are
 * required, per Phase 12.5's confirmed runtime evidence — the AUT surfaces a
 * "Value looks invalid." error on each of Cardholder Name, Card Number,
 * Expiration Date, and Security Code when empty and blurred.
 * <p>
 * {@code cardNumber} and {@code expirationDate} hold the raw value to type;
 * {@code expectedCardNumber}/{@code expectedExpirationDate} hold the
 * auto-formatted value the AUT's custom widgets return from {@code getText()}
 * (space-grouped digits, slash-inserted date — confirmed Phase 12.5). Kept
 * as explicit dataset fields rather than computed in code, since the
 * formatting is a widget implementation detail this dataset simply records,
 * not business logic the framework should derive. Cardholder Name and
 * Security Code use plain {@code EditText} fields (Phase 12.5, confirmed no
 * reformatting), so no separate expected-value field is needed for either.
 */
public record PaymentCard(
        @Required String cardholderName,
        @Required String cardNumber,
        @Required String expectedCardNumber,
        @Required String expirationDate,
        @Required String expectedExpirationDate,
        @Required String securityCode
) implements Serializable {
}
