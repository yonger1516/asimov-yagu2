package com.seven.asimov.test.tool.validation;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

/**
 * VerificationPattern.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class VerificationPattern {

    public VerificationPattern(String verificationString) {
        parse(verificationString);
    }

    private HashMap<Integer, VerificationItem> mRequestVerificationMap = new HashMap<Integer, VerificationItem>();

    public synchronized VerificationItem getRequestVerificationItem(int couner) {
        return mRequestVerificationMap.get(couner);
    }

    private HashMap<Integer, VerificationItem> mResponseVerificationMap = new HashMap<Integer, VerificationItem>();

    public synchronized VerificationItem getResponseVerificationItem(int couner) {
        return mResponseVerificationMap.get(couner);
    }

    public static Integer getIntegerParam(String string) {
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

        if (parts.length < 2) {
            return null;
        }

        return Integer.valueOf(parts[1]);
    }

    public static Integer getIntegerParamByPosition(String string, int position) {
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

        if (parts.length < 2) {
            return null;
        }

        return Integer.valueOf(parts[position]);
    }

    private void parse(String verificationString) {

        if (StringUtils.isEmpty(verificationString)) {
            return;
        }

        verificationString = verificationString.trim();

        if (StringUtils.isEmpty(verificationString)) {
            return;
        }

        int verficationIndex;

        String tagString;

        VerificationItem vi = new VerificationItem();

        try {

            // Tried using regex but seems not good idea...
            // Pattern pattern = Pattern.compile(
            // "[\\s]*(\\d+){1}[\\s]*[.]+[\\s]*(\\w+)[\\s]*[(]{1}([=+-><~]+)[,]?([a-Z])?[)]{1}[\\s]*",
            // Pattern.CASE_INSENSITIVE);
            // Matcher match = pattern.matcher(verificationString);
            //
            // if (match.find()) {
            // for (int i = 0; i < match.groupCount(); i++) {
            // String ere = match.group(i);
            // String tere = "";
            // }
            // }

            int lastIndex = 0;

            // Search "."
            int index = verificationString.indexOf(".", 0);
            if (index != -1) {

                verficationIndex = Integer.valueOf(verificationString.substring(lastIndex, index));

                lastIndex = index + 1;

                // Search "("
                index = verificationString.indexOf("(", lastIndex - 1);

                if (index != -1) {

                    tagString = verificationString.substring(lastIndex, index);

                    lastIndex = index + 1;

                    if (StringUtils.isNotEmpty(tagString)) {

                        VerificationTags tag = VerificationTags.valueOf(tagString);

                        if (tag == null) {
                            return;
                        }

                        vi.setTag(tag);

                        switch (tag) {
                            default:
                                break;

                            case VALIDATE_RESP_SIZE:
                                vi.setOperator(VerificationOperators.parse(verificationString.substring(index)));
                                vi.setSize(getIntegerParam(verificationString.substring(index)));
                                mResponseVerificationMap.put(verficationIndex, vi);
                            case VALIDATE_RESP_STATUS_CODE:
                                vi.setOperator(VerificationOperators.parse(verificationString.substring(index)));
                                vi.setStatusCode(getIntegerParam(verificationString.substring(index)));
                                mResponseVerificationMap.put(verficationIndex, vi);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
