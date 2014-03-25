package com.seven.asimov.test.tool.validation;

/**
 * The Enum Tags.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public enum VerificationTags {

    /**
     * Validate response size.
     */
    VALIDATE_RESP_SIZE("VALIDATE_RESP_SIZE"),
    /**
     * "!=".
     */
    VALIDATE_RESP_HEADERS("VALIDATE_RESP_HEADERS"),
    /**
     * Validate response headers.
     */
    VALIDATE_RESP_BODY("VALIDATE_RESP_BODY"),
    /**
     * Validate response body.
     */
    VALIDATE_RESP_STATUS_CODE("VALIDATE_RESP_STATUS_CODE"),
    /**
     * Validate response body size.
     */
    VALIDATE_RESP_BODY_SIZE("VALIDATE_RESP_BODY_SIZE"),
    /**
     * Validate response time.
     */
    VALIDATE_RESP_TIME("VALIDATE_RESP_TIME"),
    /**
     * Validate response status.
     */
    VALIDATE_STATUS("VALIDATE_STATUS");

    private String mValue;

    public void setValue(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    VerificationTags(String value) {
        this.mValue = value;
    }

}
