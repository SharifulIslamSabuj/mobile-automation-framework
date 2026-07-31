/**
 * Reusable, generic building blocks (waiting, gestures, scrolling,
 * screenshots, files, dates, randomness, device/app/permission/keyboard/toast
 * operations) — never specific to any single screen, test, or AUT business
 * flow. Driver-dependent utilities obtain the driver exclusively through
 * {@link com.mobileautomation.framework.driver.DriverProvider}, never by
 * managing a driver reference themselves. See
 * {@code docs/framework/CORE_UTILITIES_ARCHITECTURE.md} for the full
 * architecture and dependency rules.
 */
package com.mobileautomation.framework.utils;
