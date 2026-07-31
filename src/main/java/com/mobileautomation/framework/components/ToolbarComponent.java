package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** Generic toolbar: a title and a single action button (e.g. menu/cart icon), both caller-supplied locators. No Sauce Demo implementation. */
public class ToolbarComponent extends BaseComponent {

    private final By titleLocator;
    private final By actionButtonLocator;

    public ToolbarComponent(By rootLocator, By titleLocator, By actionButtonLocator) {
        super(rootLocator);
        this.titleLocator = titleLocator;
        this.actionButtonLocator = actionButtonLocator;
    }

    public String getTitle() {
        return elementActions.getText(titleLocator);
    }

    public boolean isActionButtonDisplayed() {
        return elementActions.isDisplayed(actionButtonLocator);
    }

    public void clickAction() {
        elementActions.click(actionButtonLocator);
    }
}
