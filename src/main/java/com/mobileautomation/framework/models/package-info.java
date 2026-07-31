/**
 * Plain data model / POJO classes representing domain entities used across
 * the framework, implemented in Phase 8 (Test Data Framework) as immutable
 * Java records — {@code LoginCredentials}, {@code UserProfile},
 * {@code ProductItem} — Jackson-mapped by {@code data.reader}, validated by
 * {@code data.validator.DataValidator} via {@code @Required}-annotated
 * components. Deliberately generic (no AUT-specific field or sample value).
 * See docs/framework/TEST_DATA_FRAMEWORK.md.
 */
package com.mobileautomation.framework.models;
