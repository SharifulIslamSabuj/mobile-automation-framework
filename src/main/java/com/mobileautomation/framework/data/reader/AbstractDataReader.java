package com.mobileautomation.framework.data.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileautomation.framework.exceptions.TestDataException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Shared plumbing for every {@link DataReader}: opens the classpath
 * resource (throwing {@link TestDataException} if missing), delegates
 * deserialization to the concrete format's {@link ObjectMapper} subclass,
 * and wraps any parse failure in {@link TestDataException} with the
 * original cause preserved. Concrete readers supply only their mapper and a
 * human-readable format name for error messages — this is what keeps
 * {@code JsonDataReader}/{@code YamlDataReader}/{@code PropertiesDataReader}
 * each a few lines long (DRY).
 */
public abstract class AbstractDataReader implements DataReader {

    protected abstract ObjectMapper mapper();

    protected abstract String formatName();

    @Override
    public final <T> T read(String resourcePath, Class<T> targetType) {
        try (InputStream input = openResource(resourcePath)) {
            return mapper().readValue(input, targetType);
        } catch (IOException e) {
            throw new TestDataException(
                    "Failed to parse " + formatName() + " test data resource '" + resourcePath
                            + "' into " + targetType.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private InputStream openResource(String resourcePath) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (input == null) {
            throw new TestDataException("Test data resource not found on classpath: " + resourcePath);
        }
        return input;
    }
}
