package com.seven.asimov.it.utils.logcat.tasks.failoverTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.TryingToConnectToRelayWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TryingToConnectToRelayTask extends Task<TryingToConnectToRelayWrapper> {

    private static final String TRYING_TO_CONNECT_TO_RELAY_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Trying to connect to relay";

    private static final Pattern tryingToConnectToRelayPattern = Pattern.compile(TRYING_TO_CONNECT_TO_RELAY_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected TryingToConnectToRelayWrapper parseLine(String line) {
        Matcher matcher = tryingToConnectToRelayPattern.matcher(line);
        if (matcher.find()) {
            TryingToConnectToRelayWrapper wrapper = new TryingToConnectToRelayWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
