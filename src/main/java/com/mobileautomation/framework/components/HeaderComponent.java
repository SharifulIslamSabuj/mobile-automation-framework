package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** Generic screen header: a title and an optional back button, both caller-supplied locators. No Sauce Demo implementation. */
public class HeaderComponent extends BaseComponent {

    private final By titleLocator;
    private final By backButtonLocator;

    public HeaderComponent(By rootLocator, By titleLocator, By backButtonLocator) {
        super(rootLocator);
        this.titleLocator = titleLocator;
        this.backButtonLocator = backButtonLocator;
    }

    public String getTitle() {
        return elementActions.getText(titleLocator);
    }

    public boolean isBackButtonDisplayed() {
        return elementActions.isDisplayed(backButtonLocator);
    }

    public void clickBack() {
        elementActions.click(backButtonLocator);
    }
}
