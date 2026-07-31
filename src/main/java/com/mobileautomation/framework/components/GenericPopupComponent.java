package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** The most generic popup shape: a root region and a close control, both caller-supplied locators. Use this when a popup fits neither {@link ConfirmationDialogComponent} nor {@link AlertDialogComponent}. */
public class GenericPopupComponent extends BaseComponent {

    private final By closeButtonLocator;

    public GenericPopupComponent(By rootLocator, By closeButtonLocator) {
        super(rootLocator);
        this.closeButtonLocator = closeButtonLocator;
    }

    public void close() {
        elementActions.click(closeButtonLocator);
    }
}
