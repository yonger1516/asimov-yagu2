package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollWrapper;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartPollTask extends Task<StartPollWrapper> {
    private static final String TAG = StartPollTask.class.getSimpleName();

    private static final String START_POLL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Subscription.*\\[([-0-9]*).*RR.*\\[([-0-9]*).*status POLLING";
    private static final Pattern startPollPattern = Pattern.compile(START_POLL_REGEXP);

    @Override
    protected StartPollWrapper parseLine(String line) {
        Matcher matcher = startPollPattern.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            StartPollWrapper wrapper = new StartPollWrapper(0, matcher.group(3), matcher.group(4));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}