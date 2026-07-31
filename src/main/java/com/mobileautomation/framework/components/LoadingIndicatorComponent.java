package com.mobileautomation.framework.components;

import com.mobileautomation.framework.utils.WaitUtility;
import org.openqa.selenium.By;

import java.time.Duration;

/** Generic loading/progress indicator: waits for the caller-supplied root locator to disappear. No Sauce Demo implementation. */
public class LoadingIndicatorComponent extends BaseComponent {

    public LoadingIndicatorComponent(By rootLocator) {
        super(rootLocator);
    }

    public void waitUntilHidden() {
        WaitUtility.waitForInvisibility(rootLocator);
    }

    public void waitUntilHidden(Duration timeout) {
        WaitUtility.waitForInvisibility(rootLocator, timeout);
    }
}
