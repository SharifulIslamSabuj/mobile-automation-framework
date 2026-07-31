package com.mobileautomation.framework.components;

import com.mobileautomation.framework.core.ElementActions;
import org.openqa.selenium.By;

/**
 * Base for every reusable UI component abstraction in this package. A
 * component is scoped by a caller-supplied root locator — never a
 * hardcoded, application-specific one — so the same component class is
 * reusable across any screen or any AUT. Composes {@link ElementActions}
 * rather than duplicating wait/interaction logic.
 */
public abstract class BaseComponent {

    protected final ElementActions elementActions;
    protected final By rootLocator;

    protected BaseComponent(By rootLocator) {
        this.rootLocator = rootLocator;
        this.elementActions = new ElementActions();
    }

    public boolean isDisplayed() {
        return elementActions.isDisplayed(rootLocator);
    }
}
