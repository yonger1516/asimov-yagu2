package com.seven.asimov.it.utils.logcat.tasks;


import com.seven.asimov.it.utils.logcat.wrappers.OCInitializationWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCInitializationTask extends Task<OCInitializationWrapper> {

    private static final String TAG = OCInitializationTask.class.getSimpleName();

    private static final String OC_INITIALIZATION_REASON_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Starting: com.seven.asimov";
    private static final Pattern ocInitializationPattern = Pattern.compile(OC_INITIALIZATION_REASON_REGEXP, Pattern.CASE_INSENSITIVE);

    protected OCInitializationWrapper parseLine(String line) {
        Matcher matcher1 = ocInitializationPattern.matcher(line);
        if (matcher1.find()) {
            OCInitializationWrapper ocInitializationWrapper = new OCInitializationWrapper();
            setTimestampToWrapper(ocInitializationWrapper, matcher1);
            return ocInitializationWrapper;
        }
        return null;
    }
}