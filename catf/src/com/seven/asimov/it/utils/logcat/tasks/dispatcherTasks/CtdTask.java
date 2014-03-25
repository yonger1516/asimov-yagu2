package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CTDWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CtdTask extends Task<CTDWrapper> {

    private static final String TAG = CtdTask.class.getSimpleName();

    private static final String EXECUTED_TASK_HTTP_CTD_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*(Executed\\stask\\sHTTP\\sCTD)";
    private static final Pattern executedTaskHttpsCtdPattern = Pattern.compile(EXECUTED_TASK_HTTP_CTD_REGEXP, Pattern.CASE_INSENSITIVE);

    protected CTDWrapper parseLine(String line) {
        Matcher matcher = executedTaskHttpsCtdPattern.matcher(line);
        if (matcher.find()) {
            CTDWrapper wrapper = new CTDWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
