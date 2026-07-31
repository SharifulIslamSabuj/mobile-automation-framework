package com.mobileautomation.framework.components;

import com.mobileautomation.framework.exceptions.ElementActionException;
import org.openqa.selenium.By;

import java.util.Map;

/**
 * Generic bottom navigation bar. Tab locators are supplied by the caller as
 * a name-to-locator map — this class knows nothing about how many tabs
 * exist or what they are named for any specific AUT.
 */
public class BottomNavigationComponent extends BaseComponent {

    private final Map<String, By> tabLocators;

    public BottomNavigationComponent(By rootLocator, Map<String, By> tabLocators) {
        super(rootLocator);
        this.tabLocators = Map.copyOf(tabLocators);
    }

    public void selectTab(String tabName) {
        elementActions.click(locatorFor(tabName));
    }

    public boolean isTabSelected(String tabName) {
        return elementActions.isSelected(locatorFor(tabName));
    }

    public boolean isTabDisplayed(String tabName) {
        return elementActions.isDisplayed(locatorFor(tabName));
    }

    private By locatorFor(String tabName) {
        By locator = tabLocators.get(tabName);
        if (locator == null) {
            throw new ElementActionException("No locator registered for bottom navigation tab: '" + tabName + "'");
        }
        return locator;
    }
}
