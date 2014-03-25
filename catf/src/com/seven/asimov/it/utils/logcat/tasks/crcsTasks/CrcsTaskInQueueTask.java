package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CrcsTaskInQueueWrapper;

public class CrcsTaskInQueueTask extends Task<CrcsTaskInQueueWrapper> {

    private static final String TAG = CrcsTaskInQueueTask.class.getSimpleName();

    private static final String CRCS_TASK_IN_QUEUE_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*status=([A-Z_]*).*type=([A-Z_]*).*token=([0-9]*).*added to the queue";

    private static final Pattern crcsTaskInQueuePattern = Pattern.compile(CRCS_TASK_IN_QUEUE_REGEXP, Pattern.CASE_INSENSITIVE);


    @Override
    protected CrcsTaskInQueueWrapper parseLine(String line) {
        Matcher matcher = crcsTaskInQueuePattern.matcher(line);
        if (matcher.find()) {
            CrcsTaskInQueueWrapper wrapper = new CrcsTaskInQueueWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setStatus(matcher.group(3));
            wrapper.setType(matcher.group(4));
            wrapper.setToken(matcher.group(5));
            //if ("REPORT_TRANSFER".equals())
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
