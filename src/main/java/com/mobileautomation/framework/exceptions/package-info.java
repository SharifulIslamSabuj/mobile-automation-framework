/**
 * Custom framework exception types, rooted at the abstract
 * {@code FrameworkException} (added Phase 6 — Cross-Cutting Infrastructure)
 * so every framework-thrown exception shares one common, catchable type.
 * {@code ConfigurationException}, {@code DriverInitializationException},
 * {@code UtilityOperationException}, and {@code ReportingException} each
 * extend it. See docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md.
 */
package com.mobileautomation.framework.exceptions;
