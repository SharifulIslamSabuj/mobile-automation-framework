package com.mobileautomation.framework.data.reader;

/**
 * Common contract for every test-data format reader (JSON, YAML, Properties
 * today; CSV/Excel/Database can be added later as new implementations
 * without any change to {@code data.loader.DataLoader} beyond registering
 * the new reader). Deliberately not consumed directly by future tests — see
 * {@code data.manager.TestDataManager}.
 */
public interface DataReader {

    /**
     * @param resourcePath classpath-relative path to the data resource (e.g. {@code "testdata/common/login/credentials.json"})
     * @param targetType   the strongly-typed POJO/record to deserialize into — never {@code Map<String, Object>}
     * @throws com.mobileautomation.framework.exceptions.TestDataException if the resource is missing or malformed
     */
    <T> T read(String resourcePath, Class<T> targetType);
}
