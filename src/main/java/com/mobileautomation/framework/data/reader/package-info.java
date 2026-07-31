/**
 * The common test-data reader contract ({@code DataReader}) and its shared
 * plumbing ({@code AbstractDataReader}). Format-specific implementations
 * live in the {@code json}/{@code yaml}/{@code properties} sub-packages;
 * future formats (CSV/Excel/Database) are added the same way, without
 * changing {@code data.loader.DataLoader}'s consumer-facing API. See
 * docs/framework/TEST_DATA_FRAMEWORK.md.
 */
package com.mobileautomation.framework.data.reader;
