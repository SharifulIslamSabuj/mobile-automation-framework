/**
 * Core framework abstractions shared across the driver, page, and test
 * layers, implemented in Phase 7 (Base Framework): {@code BasePage} (every
 * future Page Object's base), {@code BaseTest} (every future Test Class's
 * base), {@code ElementActions} (reusable Selenium/Appium interaction
 * wrapper), and {@code NavigationHelper} (generic navigation operations).
 * None of these four classes contains application-specific or business
 * logic. {@code ElementActions.findAll(By)} was added in Phase 9.2 —
 * a minimal, additive extension needed for RecyclerView/list-shaped
 * screens (MA-LOC-001 §19.1), following the exact same pattern as every
 * pre-existing method. See docs/framework/BASE_FRAMEWORK_ARCHITECTURE.md.
 */
package com.mobileautomation.framework.core;
