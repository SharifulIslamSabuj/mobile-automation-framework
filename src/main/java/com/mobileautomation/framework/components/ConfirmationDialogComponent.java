package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** Generic two-option confirmation dialog: a message, a confirm button, and a cancel button, all caller-supplied locators. No AUT-specific dialog. */
public class ConfirmationDialogComponent extends BaseComponent {

    private final By messageLocator;
    private final By confirmButtonLocator;
    private final By cancelButtonLocator;

    public ConfirmationDialogComponent(By rootLocator, By messageLocator, By confirmButtonLocator, By cancelButtonLocator) {
        super(rootLocator);
        this.messageLocator = messageLocator;
        this.confirmButtonLocator = confirmButtonLocator;
        this.cancelButtonLocator = cancelButtonLocator;
    }

    public String getMessage() {
        return elementActions.getText(messageLocator);
    }

    public void confirm() {
        elementActions.click(confirmButtonLocator);
    }

    public void cancel() {
        elementActions.click(cancelButtonLocator);
    }
}
