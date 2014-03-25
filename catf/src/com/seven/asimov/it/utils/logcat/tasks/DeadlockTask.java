package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.DeadlockWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeadlockTask extends Task<DeadlockWrapper> {

    private static final String TAG = DeadlockTask.class.getSimpleName();

    private static final String DEADLOCK_REGEX = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Deadlock in threadpool detected:.*";

    private static final Pattern deadlockPattern = Pattern.compile(DEADLOCK_REGEX, Pattern.CASE_INSENSITIVE);

    @Override
    protected DeadlockWrapper parseLine(String line) {
        Matcher matcher = deadlockPattern.matcher(line);
        if (matcher.find()) {
            DeadlockWrapper wrapper = new DeadlockWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
