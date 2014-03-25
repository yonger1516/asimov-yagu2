package com.seven.asimov.test.tool.validation;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * VerificationOperators enum.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public enum VerificationOperators {

    /**
     * "==".
     */
    EQUAL_TO("=="),
    /**
     * "!=".
     */
    NOT_EQUAL_TO("!="),
    /**
     * ">".
     */
    GREATER_THAN(">"),
    /**
     * ">=".
     */
    GREATER_THAN_OR_EQUAL_TO(">="),
    /**
     * "<".
     */
    LESS_THAN("<"),
    /**
     * "<=".
     */
    LESS_THAN_OR_EQUAL_TO("<="),
    /**
     * "~".
     */
    CONTAINS("~"),
    /**
     * "~Num".
     */
    CONTAINS_EXACT_NUMBER_OF_INSTANCES("~Num"),
    /**
     * "~!".
     */
    BEGINS_WITH("~!"),
    /**
     * "!~".
     */
    ENDS_WITH("!~"),
    /**
     * "+-Num".
     */
    PRECISION_WITHIN("+-Num"),
    /**
     * "+Num".
     */
    PRECISION_AS_HIGH_AS("+Num"),
    /**
     * "-Num".
     */
    PRECISION_AS_LOW_AS("-Num"),
    /**
     * "SCREEN_ON".
     */
    SCREEN_ON("SCREEN_ON"),
    /**
     * "SCREEN_OFF".
     */
    SCREEN_OFF("SCREEN_OFF"),
    /**
     * "WIFI_ON".
     */
    WIFI_ON("WIFI_ON"),
    /**
     * "WIFI_ON".
     */
    WIFI_OFF("WIFI_OFF"),
    /**
     * "AIRPLANE_ON".
     */
    AIRPLANE_ON("AIRPLANE_ON"),
    /**
     * "AIRPLANE_OFF".
     */
    AIRPLANE_OFF("AIRPLANE_OFF"),
    /**
     * "TCPDUMP_ON".
     */
    TCPDUMP_ON("TCPDUMP_ON"),
    /**
     * "TCPDUMP_OFF".
     */
    TCPDUMP_OFF("TCPDUMP_OFF");

    private String mValue;

    public void setValue(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    VerificationOperators(String value) {
        this.mValue = value;
    }

    private static final Map<String, VerificationOperators> VERIFICATION_OPERATORS;

    static {
        VERIFICATION_OPERATORS = new HashMap<String, VerificationOperators>();
        VERIFICATION_OPERATORS.put(EQUAL_TO.getValue(), EQUAL_TO);
        VERIFICATION_OPERATORS.put(NOT_EQUAL_TO.getValue(), NOT_EQUAL_TO);
        VERIFICATION_OPERATORS.put(GREATER_THAN.getValue(), GREATER_THAN);
        VERIFICATION_OPERATORS.put(GREATER_THAN_OR_EQUAL_TO.getValue(), GREATER_THAN_OR_EQUAL_TO);
        VERIFICATION_OPERATORS.put(LESS_THAN.getValue(), LESS_THAN);
        VERIFICATION_OPERATORS.put(LESS_THAN_OR_EQUAL_TO.getValue(), LESS_THAN_OR_EQUAL_TO);
        VERIFICATION_OPERATORS.put(CONTAINS.getValue(), CONTAINS);
        VERIFICATION_OPERATORS.put(CONTAINS_EXACT_NUMBER_OF_INSTANCES.getValue(), CONTAINS_EXACT_NUMBER_OF_INSTANCES);
        VERIFICATION_OPERATORS.put(BEGINS_WITH.getValue(), BEGINS_WITH);
        VERIFICATION_OPERATORS.put(ENDS_WITH.getValue(), ENDS_WITH);
        VERIFICATION_OPERATORS.put(PRECISION_WITHIN.getValue(), PRECISION_WITHIN);
        VERIFICATION_OPERATORS.put(PRECISION_AS_HIGH_AS.getValue(), PRECISION_AS_HIGH_AS);
        VERIFICATION_OPERATORS.put(PRECISION_AS_LOW_AS.getValue(), PRECISION_AS_LOW_AS);
    }

    public static VerificationOperators parse(String string) {
        int index1 = string.indexOf("(");
        if (index1 == -1) {
            return null;
        }
        int index2 = string.indexOf(")", index1 + 1);
        if (index2 == -1) {
            return null;
        }
        String result = string.substring(index1 + 1, index2);

        if (StringUtils.isEmpty(result)) {
            return null;
        }

        String[] parts = result.split(",", -1);

        return VERIFICATION_OPERATORS.get(parts[0]);
    }
}
