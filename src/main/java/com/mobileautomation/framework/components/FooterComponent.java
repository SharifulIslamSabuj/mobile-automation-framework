package com.mobileautomation.framework.components;

import org.openqa.selenium.By;

/** Generic screen footer: a root region and a caller-supplied info-text locator. No Sauce Demo implementation. */
public class FooterComponent extends BaseComponent {

    private final By infoTextLocator;

    public FooterComponent(By rootLocator, By infoTextLocator) {
        super(rootLocator);
        this.infoTextLocator = infoTextLocator;
    }

    public String getInfoText() {
        return elementActions.getText(infoTextLocator);
    }
}
