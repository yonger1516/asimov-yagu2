package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.CreatedTaskSchedulerTaskWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreatedTaskSchedulerTaskTask extends Task<CreatedTaskSchedulerTaskWrapper> {

    private static final String TAG = CreatedTaskSchedulerTaskTask.class.getSimpleName();

    private static final String WORKER_TASK_DAILY_SENDING_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Created task OC Scheduler Task \\{0x([0-9a-f]*)\\}, delay\\s([0-9]*).*worker task Daily CRCS Sending Task";

    private static final Pattern workerTaskDailySendingPattern = Pattern.compile(WORKER_TASK_DAILY_SENDING_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public CreatedTaskSchedulerTaskWrapper parseLine(String line) {
        Matcher matcher = workerTaskDailySendingPattern.matcher(line);
        if (matcher.find()) {
            CreatedTaskSchedulerTaskWrapper wrapper = new CreatedTaskSchedulerTaskWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setId(Integer.parseInt(matcher.group(3), 16));
            wrapper.setDelay(Integer.parseInt(matcher.group(4)));
            //Log.d(TAG, "parseLine found line " + matcher.group(1));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
