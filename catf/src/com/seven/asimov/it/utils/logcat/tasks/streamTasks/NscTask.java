package com.seven.asimov.it.utils.logcat.tasks.streamTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.NSCWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NscTask extends Task<NSCWrapper> {

    private static final String TAG = NscTask.class.getSimpleName();

    private static final String NSC_REASON_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*reason.-([0-9]*)";
    private static final Pattern nscReasonPattern = Pattern.compile(NSC_REASON_REGEXP, Pattern.CASE_INSENSITIVE);

    protected NSCWrapper parseLine(String line) {
        Matcher matcher1 = nscReasonPattern.matcher(line);
        if (matcher1.find()) {
            NSCWrapper nscWrapper = new NSCWrapper();
            setTimestampToWrapper(nscWrapper, matcher1);
            nscWrapper.setReason(0 - Integer.parseInt(matcher1.group(3)));
            return nscWrapper;
        }
        return null;
    }
}
