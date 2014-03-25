package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollRequestWrapper;

import java.lang.Override;
import java.lang.String;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartPollRequestTask extends Task<StartPollRequestWrapper> {

    private static final String TAG = StartPollRequestTask.class.getSimpleName();
    private static final String START_POLL_REQUEST_REGEXP = "([A-Z]?)/Asimov::Java::AbstractZ7TransportMultiplexer\\(\\s?[0-9]+\\): (201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).* Sending a traffic harmonizer start polling request";
    private static final Pattern startPollRequestPattern = Pattern.compile(START_POLL_REQUEST_REGEXP,Pattern.CASE_INSENSITIVE);

    @Override
    protected StartPollRequestWrapper parseLine(String line) {
        Matcher matcher = startPollRequestPattern.matcher(line);
        if (matcher.find()) {
            StartPollRequestWrapper wrapper = new StartPollRequestWrapper();
            wrapper.setLogLevel(matcher.group(1));
            setTimestampToWrapper(wrapper, matcher, 2, 3);
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
