package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** Generic single-button alert dialog: a message and a dismiss button, both caller-supplied locators. No AUT-specific dialog. */
public class AlertDialogComponent extends BaseComponent {

    private final By messageLocator;
    private final By dismissButtonLocator;

    public AlertDialogComponent(By rootLocator, By messageLocator, By dismissButtonLocator) {
        super(rootLocator);
        this.messageLocator = messageLocator;
        this.dismissButtonLocator = dismissButtonLocator;
    }

    public String getMessage() {
        return elementActions.getText(messageLocator);
    }

    public void dismiss() {
        elementActions.click(dismissButtonLocator);
    }
}
