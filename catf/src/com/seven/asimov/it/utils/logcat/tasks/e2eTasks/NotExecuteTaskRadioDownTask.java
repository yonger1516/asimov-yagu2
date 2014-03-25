package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.crcsTasks.CrcsAccumulatedTask;
import com.seven.asimov.it.utils.logcat.wrappers.NotExecuteTaskRadioDownWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotExecuteTaskRadioDownTask extends Task<NotExecuteTaskRadioDownWrapper> {

    private static final String TAG = CrcsAccumulatedTask.class.getSimpleName();

    private static final String RADIO_DOWN_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Not executing task, radio is down.";

    private static final Pattern radioDownPattern = Pattern.compile(RADIO_DOWN_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public NotExecuteTaskRadioDownWrapper parseLine(String line) {

        Matcher matcher = radioDownPattern.matcher(line);
        if (matcher.find()) {
            NotExecuteTaskRadioDownWrapper wrapper = new NotExecuteTaskRadioDownWrapper();
            setTimestampToWrapper(wrapper, matcher);
            //wrapper.setCount(Integer.parseInt(matcher.group(3)));
            return wrapper;
        }
        return null;
    }

}
