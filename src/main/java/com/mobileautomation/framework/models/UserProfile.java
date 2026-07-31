package com.mobileautomation.framework.models;

import com.mobileautomation.framework.data.validator.Required;

import java.io.Serializable;

/** Generic user-profile shape (registration/account forms). No application-specific meaning. */
public record UserProfile(
        @Required String firstName,
        @Required String lastName,
        @Required String email,
        String phone,
        String username
) implements Serializable {
}
