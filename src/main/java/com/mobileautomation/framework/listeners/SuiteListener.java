package com.mobileautomation.framework.listeners;

import com.mobileautomation.framework.logging.LogManager;
import com.mobileautomation.framework.reporting.ReportProvider;
import org.slf4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/** Drives the report lifecycle at suite granularity: initialize on suite start, flush on suite finish. */
public class SuiteListener implements ISuiteListener {

    private static final Logger LOGGER = LogManager.getLogger(SuiteListener.class);

    @Override
    public void onStart(ISuite suite) {
        LOGGER.info("Suite started: {}", suite.getName());
        ReportProvider.initializeReport();
    }

    @Override
    public void onFinish(ISuite suite) {
        ReportProvider.flushReport();
        LOGGER.info("Suite finished: {}", suite.getName());
    }
}
