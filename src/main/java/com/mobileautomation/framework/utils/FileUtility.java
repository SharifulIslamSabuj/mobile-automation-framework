package com.mobileautomation.framework.utils;

import com.mobileautomation.framework.exceptions.UtilityOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Reusable file-system operations built on Java NIO ({@code java.nio.file}).
 * Stateless, driver- and configuration-independent. Every checked
 * {@link IOException} is wrapped in {@link UtilityOperationException} with
 * the path involved, per the selective-wrapping decision in
 * docs/framework/CORE_UTILITIES_ARCHITECTURE.md.
 */
public final class FileUtility {

    private FileUtility() {
    }

    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Creates the given directory, including any missing parent directories.
     * Safe/idempotent — does nothing if the directory already exists.
     */
    public static Path createDirectoryIfNotExists(Path directory) {
        try {
            return Files.createDirectories(directory);
        } catch (IOException e) {
            throw new UtilityOperationException("Failed to create directory: " + directory, e);
        }
    }

    /** Copies {@code source} to {@code target}, creating {@code target}'s parent directory first if needed, overwriting any existing file at {@code target}. */
    public static Path copyFile(Path source, Path target) {
        try {
            if (target.getParent() != null) {
                createDirectoryIfNotExists(target.getParent());
            }
            return Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UtilityOperationException("Failed to copy file from " + source + " to " + target, e);
        }
    }

    /** @return {@code true} if a file existed and was deleted, {@code false} if it did not exist — never throws for a missing file. */
    public static boolean deleteIfExists(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UtilityOperationException("Failed to delete file: " + path, e);
        }
    }

    public static Path buildPath(String first, String... more) {
        return Paths.get(first, more);
    }

    public static Path createTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix, suffix);
        } catch (IOException e) {
            throw new UtilityOperationException(
                    "Failed to create a temporary file with prefix '" + prefix + "' and suffix '" + suffix + "'", e);
        }
    }
}
