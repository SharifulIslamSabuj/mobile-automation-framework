package com.mobileautomation.framework.data.factory;

import com.mobileautomation.framework.data.manager.TestDataManager;
import com.mobileautomation.framework.models.PaymentCard;

/** Environment-aware Payment Card dataset (MA-TDD-001 §8.3, TD-003) behind one API — mirrors {@link ShippingDataFactory}/{@link ProductDataFactory}'s established shape. No business logic. */
public final class PaymentDataFactory {

    private static final String CARD_RESOURCE = "payment/card.json";

    private final TestDataManager testDataManager;

    public PaymentDataFactory(TestDataManager testDataManager) {
        this.testDataManager = testDataManager;
    }

    /** The canned, environment-aware Payment Card dataset (TD-003), falling back to the common dataset. */
    public PaymentCard standardCard() {
        return testDataManager.loadEnvironmentData(CARD_RESOURCE, PaymentCard.class);
    }
}
