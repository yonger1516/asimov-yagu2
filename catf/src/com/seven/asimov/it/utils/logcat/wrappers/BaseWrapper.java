package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * This wrapper should be used on pair with  if only the presence of entries specified by regexp
 * should be checked in the log, that is the content of entries does not matter.
 */
public class BaseWrapper extends LogEntryWrapper {
    @Override
    public String toString() {
        return "BaseWrapper: " + super.toString();
    }
}
