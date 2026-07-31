package com.mobileautomation.framework.reporting;

import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.utils.ScreenshotUtility;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Reporting-layer wrapper around {@link ScreenshotUtility} used by listener
 * hooks (e.g. on test failure). Deliberately never throws: a screenshot
 * capture failure during failure handling must never mask the real test
 * failure that triggered it, so any exception from the underlying capture
 * is logged and converted to {@link Optional#empty()} rather than
 * propagated. This is a documented deviation from the general "do not
 * swallow exceptions" principle — see the Architectural Decisions section of
 * docs/framework/CROSS_CUTTING_INFRASTRUCTURE.md.
 * <p>
 * Does not attach screenshots to a report — that is out of scope for this
 * phase.
 */
public final class ScreenshotManager {

    private static final Logger LOGGER = LogManager.getLogger(ScreenshotManager.class);

    private ScreenshotManager() {
    }

    /**
     * Captures a screenshot, returning the saved path on success or
     * {@link Optional#empty()} if capture failed for any reason (e.g. no
     * active driver on this thread). Never throws.
     */
    public static Optional<Path> captureScreenshot(String namePrefix) {
        try {
            Path path = ScreenshotUtility.captureScreenshot(namePrefix);
            LOGGER.info("Screenshot captured: {}", path);
            return Optional.of(path);
        } catch (RuntimeException e) {
            LOGGER.warn("Screenshot capture failed for prefix '{}': {}", namePrefix, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
