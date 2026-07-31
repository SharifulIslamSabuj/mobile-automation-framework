package com.mobileautomation.framework.listeners;

import com.mobileautomation.framework.config.ConfigReader;
import com.mobileautomation.framework.logging.LogManager;
import org.slf4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Generic, business-agnostic retry policy: retries a failed test up to
 * {@link ConfigReader#getRetryCount()} times. Reads the configured count on
 * every {@link #retry(ITestResult)} call (not cached at construction) since
 * TestNG instantiates one {@link RetryAnalyzer} per test method and the
 * count is cheap to re-read.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger LOGGER = LogManager.getLogger(RetryAnalyzer.class);

    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        int maxRetries = ConfigReader.getInstance().getRetryCount();
        if (maxRetries <= 0 || attempt >= maxRetries) {
            return false;
        }
        attempt++;
        LOGGER.warn("Retrying test '{}' — attempt {} of {}.", result.getName(), attempt, maxRetries);
        return true;
    }
}
