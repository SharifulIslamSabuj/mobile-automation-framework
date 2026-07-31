package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.driver.DriverProvider;
import com.mobileautomation.framework.exceptions.UtilityOperationException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.nio.file.Path;

/**
 * Screenshot capture with timestamped, unique file naming. Deliberately has
 * no dependency on the {@code reporting} package — attaching a screenshot to
 * a report is a later phase's concern; this class only knows how to
 * capture one and return where it was saved. The screenshot directory is
 * read from the Configuration Layer ({@link ConfigReader#getScreenshotDirectory()}),
 * never hardcoded. Reuses {@link FileUtility} for directory/copy handling
 * and {@link DateUtility}/{@link RandomUtility} for naming, rather than
 * duplicating that logic.
 */
public final class ScreenshotUtility {

    private static final String FILE_EXTENSION = ".png";

    private ScreenshotUtility() {
    }

    /**
     * Captures a screenshot of the current driver state and saves it under
     * the configured screenshot directory with a unique, timestamped name.
     *
     * @param namePrefix a short, descriptive prefix (e.g. a test/step name); must not be null or blank
     * @return the path the screenshot was saved to
     */
    public static Path captureScreenshot(String namePrefix) {
        File rawScreenshot = ((TakesScreenshot) DriverProvider.getDriver()).getScreenshotAs(OutputType.FILE);
        return persistScreenshot(rawScreenshot, namePrefix);
    }

    /**
     * Copies an already-captured screenshot file into the configured
     * screenshot directory under a unique, timestamped name. Split out from
     * {@link #captureScreenshot(String)} so the file-naming/persistence
     * logic can be exercised independently of a live driver session.
     */
    static Path persistScreenshot(File sourceScreenshotFile, String namePrefix) {
        if (namePrefix == null || namePrefix.isBlank()) {
            throw new UtilityOperationException("Screenshot name prefix must not be null or blank.");
        }

        String fileName = buildFileName(namePrefix);
        Path targetDirectory = FileUtility.buildPath(ConfigReader.getInstance().getScreenshotDirectory());
        Path targetPath = targetDirectory.resolve(fileName);

        return FileUtility.copyFile(sourceScreenshotFile.toPath(), targetPath);
    }

    private static String buildFileName(String namePrefix) {
        return sanitize(namePrefix) + "_" + DateUtility.timestampForFilename() + "_"
                + RandomUtility.randomAlphanumeric(6) + FILE_EXTENSION;
    }

    /** Strips characters that are unsafe in a file name on common filesystems. */
    private static String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
