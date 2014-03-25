package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ConnectionFailedRelayAvaliableWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionFailedRelayAvaliableTask extends Task<ConnectionFailedRelayAvaliableWrapper> {
    private static final String TAG = ConnectionFailedRelayAvaliableWrapper.class.getSimpleName();

    private static final String CONNECTION_FAILED_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*relay_connection,event,CONNECT_FAILED.*";
    private static Pattern connectionFailedPattern = Pattern.compile(CONNECTION_FAILED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected ConnectionFailedRelayAvaliableWrapper parseLine(String line) {
        Matcher matcher = connectionFailedPattern.matcher(line);
        if (matcher.find()) {
            ConnectionFailedRelayAvaliableWrapper wrapper = new ConnectionFailedRelayAvaliableWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
