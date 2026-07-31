package com.mobileautomation.framework.listeners;

import com.mobileautomation.framework.logging.LogManager;
import org.slf4j.Logger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Lightweight, DEBUG-level observability of individual method invocations
 * (including {@code @Before}/{@code @After} configuration methods, not just
 * {@code @Test} methods) — included because per-method timing is genuinely
 * useful for diagnosing slow setup/teardown, which {@link TestListener}
 * (test-method-only) cannot see.
 */
public class MethodListener implements IInvokedMethodListener {

    private static final Logger LOGGER = LogManager.getLogger(MethodListener.class);

    private static final ThreadLocal<Long> METHOD_START_TIME = new ThreadLocal<>();

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        METHOD_START_TIME.set(System.currentTimeMillis());
        LOGGER.debug("Invoking method: {}", method.getTestMethod().getMethodName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        Long startTime = METHOD_START_TIME.get();
        long durationMs = (startTime == null) ? -1 : System.currentTimeMillis() - startTime;
        METHOD_START_TIME.remove();
        LOGGER.debug("Completed method: {} ({} ms)", method.getTestMethod().getMethodName(), durationMs);
    }
}
