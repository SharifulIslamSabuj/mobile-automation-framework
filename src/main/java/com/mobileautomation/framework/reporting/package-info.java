/**
 * ExtentReports integration and report lifecycle management, implemented in
 * Phase 6 (Cross-Cutting Infrastructure): {@code ExtentReportManager}
 * (package-private) owns the shared report and per-thread test node;
 * {@code ReportProvider} is the public facade; {@code ScreenshotManager}
 * wraps {@code utils.ScreenshotUtility} for listener failure hooks.
 * Prepares reporting infrastructure only — no business test reporting yet.
 * See docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md.
 */
package com.mobileautomation.framework.reporting;
