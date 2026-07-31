/**
 * TestNG listener implementations, implemented in Phase 6 (Cross-Cutting
 * Infrastructure): {@code SuiteListener} (report init/flush),
 * {@code TestListener} (per-test report node + failure screenshot hook),
 * {@code MethodListener} (DEBUG-level per-method timing), and
 * {@code RetryAnalyzer} (configuration-driven retry). Observes execution
 * only — contains no Page Object or business-assertion logic.
 * See docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md.
 */
package com.mobileautomation.framework.listeners;
