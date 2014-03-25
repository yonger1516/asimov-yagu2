package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.SocketError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocketErrorTask extends Task<SocketError> {

    private static final String TAG = SocketErrorTask.class.getName();

    private static final String OUT_SOCKET_ERROR_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*(OUT socket error)";
    private static final String OUT_SOCKET_ERROR__CLOSED_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*(OUT socket closed by peer)";

    private static final Pattern outSocketErrorPattern = Pattern.compile(OUT_SOCKET_ERROR_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern outSocketErrorClosedPattern = Pattern.compile(OUT_SOCKET_ERROR__CLOSED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public SocketError parseLine(String line) {
        Matcher matcher = outSocketErrorPattern.matcher(line);
        if (matcher.find()) {
            SocketError socketError = new SocketError();
            setTimestampToWrapper(socketError, matcher);
            socketError.setErrorType(SocketError.ErrorType.OUT_SOCKET_ERROR);
            return socketError;
        }
        matcher = outSocketErrorClosedPattern.matcher(line);
        if (matcher.find()) {
            SocketError socketError = new SocketError();
            setTimestampToWrapper(socketError, matcher);
            socketError.setErrorType(SocketError.ErrorType.OUT_SOCKET_CLOSED);

            return socketError;
        }
        return null;
    }

}