package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.Z7notExecutingRadioDownWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Z7notExecutingRadioDownTask extends Task<Z7notExecutingRadioDownWrapper> {
    private static final String TAG = Z7notExecutingRadioDownTask.class.getSimpleName();

    private static final String Z7_NOT_EXECUTING_TASK_RADIO_DOWN = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Not executing task, radio is down";
    private static final Pattern z7notExecutingRadioDownPattern = Pattern.compile(Z7_NOT_EXECUTING_TASK_RADIO_DOWN, Pattern.CASE_INSENSITIVE);

    @Override
    protected Z7notExecutingRadioDownWrapper parseLine(String line) {
        Matcher matcher = z7notExecutingRadioDownPattern.matcher(line);
        if (matcher.find()) {
            Z7notExecutingRadioDownWrapper wrapper = new Z7notExecutingRadioDownWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}