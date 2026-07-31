/**
 * SLF4J/Log4j2 logging entry point ({@code LogManager}), implemented in
 * Phase 6 (Cross-Cutting Infrastructure). Bridges Configuration Layer values
 * (log level, log directory) into Log4j2's system-property lookups.
 * <p>
 * Approved as the 13th permanent top-level package in Phase 8.10 (Foundation
 * Readiness Resolution) — see MA-FR-001 §1's amendment and
 * docs/framework/FOUNDATION_READINESS_RESOLUTION.md for the rationale. Kept
 * independent of {@code core}/{@code reporting} so it stays safely callable
 * from any layer, including ones "below" {@code core} in the dependency
 * chain, without a future redesign.
 */
package com.mobileautomation.framework.logging;
