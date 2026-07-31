package com.mobileautomation.framework.data.loader;

import com.mobileautomation.framework.data.reader.DataReader;
import com.mobileautomation.framework.data.reader.json.JsonDataReader;
import com.mobileautomation.framework.data.reader.properties.PropertiesDataReader;
import com.mobileautomation.framework.data.reader.yaml.YamlDataReader;
import com.mobileautomation.framework.data.validator.DataValidator;
import com.mobileautomation.framework.exceptions.TestDataException;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads a test-data resource, deserializes it into a strongly-typed POJO,
 * validates it, and caches the result — the file extension selects which
 * {@link DataReader} handles the resource, so a caller never chooses a
 * reader itself. Thread-safe: the cache is a {@link ConcurrentHashMap}, and
 * every {@link DataReader} instance held here is itself stateless.
 * <p>
 * Not a singleton itself — {@code data.manager.TestDataManager} owns one
 * instance for the whole JVM, which is what makes the cache meaningful.
 */
public final class DataLoader {

    private static final Map<String, DataReader> READERS_BY_EXTENSION = Map.of(
            "json", new JsonDataReader(),
            "yaml", new YamlDataReader(),
            "yml", new YamlDataReader(),
            "properties", new PropertiesDataReader()
    );

    private final ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<>();

    /**
     * Loads and caches {@code resourcePath} as {@code targetType}. Subsequent
     * calls with the same path and type return the cached instance without
     * re-reading the file.
     *
     * @throws TestDataException if the resource is missing, malformed, or its extension is unsupported
     * @throws com.mobileautomation.framework.exceptions.DataValidationException if the deserialized data fails required-field validation
     */
    @SuppressWarnings("unchecked")
    public <T> T load(String resourcePath, Class<T> targetType) {
        String cacheKey = resourcePath + "::" + targetType.getName();
        return (T) cache.computeIfAbsent(cacheKey, key -> readAndValidate(resourcePath, targetType));
    }

    private <T> T readAndValidate(String resourcePath, Class<T> targetType) {
        DataReader reader = resolveReader(resourcePath);
        T data = reader.read(resourcePath, targetType);
        DataValidator.validate(data);
        return data;
    }

    private DataReader resolveReader(String resourcePath) {
        String extension = extensionOf(resourcePath);
        DataReader reader = READERS_BY_EXTENSION.get(extension);
        if (reader == null) {
            throw new TestDataException("Unsupported test data file extension '" + extension
                    + "' for resource: " + resourcePath + ". Supported: " + READERS_BY_EXTENSION.keySet());
        }
        return reader;
    }

    private String extensionOf(String resourcePath) {
        int dotIndex = resourcePath.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == resourcePath.length() - 1) {
            throw new TestDataException("Test data resource has no file extension: " + resourcePath);
        }
        return resourcePath.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
