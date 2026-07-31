package com.mobileautomation.framework.models;

import com.mobileautomation.framework.data.validator.Required;

import java.io.Serializable;

/** Generic username/password pair. No application-specific meaning — a future Page Object decides which screen consumes this. */
public record LoginCredentials(@Required String username, @Required String password) implements Serializable {
}
