package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** Generic search bar: an input field and an optional clear button, both caller-supplied locators. No Sauce Demo implementation. */
public class SearchBarComponent extends BaseComponent {

    private final By inputLocator;
    private final By clearButtonLocator;

    public SearchBarComponent(By rootLocator, By inputLocator, By clearButtonLocator) {
        super(rootLocator);
        this.inputLocator = inputLocator;
        this.clearButtonLocator = clearButtonLocator;
    }

    public void search(String query) {
        elementActions.type(inputLocator, query);
    }

    public String getQuery() {
        return elementActions.getText(inputLocator);
    }

    public void clear() {
        elementActions.click(clearButtonLocator);
    }
}
