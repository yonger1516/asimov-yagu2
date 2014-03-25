package com.seven.asimov.test.tool.utils;

import junit.framework.AssertionFailedError;

public class Assert7TestToolFailure extends AssertionFailedError {

    private String fExpected;
    private String fActual;
    private String sPrefix;
    private boolean sValue;
    private boolean fValue;

    public Assert7TestToolFailure(String message) {
        super(message);
    }

    /**
     * Constructs a comparison failure.
     *
     * @param message  the identifying message or null
     * @param expected the expected string value
     * @param actual   the actual string value
     */
    public Assert7TestToolFailure(String message, String expected, String actual) {
        super(message);
        fExpected = expected;
        fActual = actual;
    }

    /**
     * Constructs a comparison failure.
     *
     * @param message     the identifying message or null
     * @param expected    the expected string value
     * @param actual      the actual string value
     * @param prefix      place between expected and actual
     * @param firstValue  is print expected value
     * @param secondValue is print actual value
     */
    public Assert7TestToolFailure(String message, String prefix, String expected, String actual, boolean firstValue, boolean secondValue) {
        super(message);
        fExpected = expected;
        fActual = actual;
        sPrefix = prefix;
        sValue = secondValue;
        fValue = firstValue;
    }

    public Assert7TestToolFailure(String message, String prefix, String expected, String actual, boolean secondValue) {
        this(message, prefix, expected, actual, true, secondValue);
    }

    /**
     * Returns "..." in place of common prefix and "..." in
     * place of common suffix between expected and actual.
     *
     * @see Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        if (fExpected == null && fActual == null)
            return new MessageComparatorUtil().compactMessage(super.getMessage());
        return new MessageComparatorUtil(fExpected, fActual).compactMessage(super.getMessage(), sPrefix, fValue, sValue);
    }

    /**
     * Gets the actual string value
     *
     * @return the actual string value
     */
    public String getActual() {
        return fActual;
    }

    /**
     * Gets the expected string value
     *
     * @return the expected string value
     */
    public String getExpected() {
        return fExpected;
    }
}
