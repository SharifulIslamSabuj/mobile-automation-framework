package com.mobileautomation.framework.data.factory;

import com.mobileautomation.framework.data.generator.DynamicDataGenerator;
import com.mobileautomation.framework.data.generator.FakerProvider;
import com.mobileautomation.framework.models.UserProfile;
import net.datafaker.Faker;

/** Combines Faker-generated and dynamic data into a ready-to-use {@link UserProfile} — no business logic. */
public final class UserDataFactory {

    public UserDataFactory() {
    }

    /** A fully synthetic, internally-consistent user profile. */
    public UserProfile fakeUserProfile() {
        Faker faker = FakerProvider.get();
        return new UserProfile(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress(),
                DynamicDataGenerator.phoneNumber(),
                DynamicDataGenerator.uniqueUsername()
        );
    }
}
