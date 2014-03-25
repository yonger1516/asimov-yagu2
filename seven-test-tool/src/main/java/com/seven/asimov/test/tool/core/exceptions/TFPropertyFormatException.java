package com.seven.asimov.test.tool.core.exceptions;

public class TFPropertyFormatException extends Exception {
    private String propertyName;

    public TFPropertyFormatException(String propertyName, String propertyValue, String expectedType) {
        super("Property name:" + propertyName + " value:" + propertyValue + " doesn't correspond to " + expectedType + "!");
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
