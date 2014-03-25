package com.seven.asimov.test.tool.core.exceptions;

public class TFPropertyNotFoundException extends Exception {
    private String propertyName;

    public TFPropertyNotFoundException(String propertyName) {
        super("Property " + propertyName + " was not found!");
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
