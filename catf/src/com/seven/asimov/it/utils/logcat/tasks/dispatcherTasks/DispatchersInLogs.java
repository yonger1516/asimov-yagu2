package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.Dispatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchersInLogs extends Task<Dispatcher> {
    private String message= "OCEngine failed reconnect to ";
    Pattern pattern = Pattern.compile(message);

    protected Dispatcher parseLine(String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            return new Dispatcher(LogcatChecks.getUnixTimeFromString(m.group(1)));
        }
        return null;
    }
}