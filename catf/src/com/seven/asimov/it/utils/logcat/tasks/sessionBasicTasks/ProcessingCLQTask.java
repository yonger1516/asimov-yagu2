package com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ProcessingCLQWrapper;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcessingCLQTask extends Task<ProcessingCLQWrapper> {
    private static final String TAG = ProcessingCLQTask.class.getSimpleName();

    private String ProcessingCLQ_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*DTRX \\[(%s)\\]: processing CLQ \\(host (%s), UID (%s), size (%s)\\)";
    private Pattern ProcessingCLQPattern;
    private static final String dtrxRegex = "\\S+";
    private static final String hostRegex = "\\S+";
    private static final String uidRegex = "\\S+";
    private static final String sizeRegex = "\\d+";

    public ProcessingCLQTask(String dtrx, String host, String uid, String size) {
        ProcessingCLQ_REGEXP = String.format(ProcessingCLQ_REGEXP,
                dtrx == null ? dtrxRegex : dtrx,
                host == null ? hostRegex : host,
                uid == null ? uidRegex : uid,
                size == null ? sizeRegex : size);

        ProcessingCLQPattern = Pattern.compile(ProcessingCLQ_REGEXP, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected ProcessingCLQWrapper parseLine(String line) {
        Matcher matcher = ProcessingCLQPattern.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            ProcessingCLQWrapper wrapper = new ProcessingCLQWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDTRX(matcher.group(3));
            wrapper.setHost(matcher.group(4));
            wrapper.setUid(matcher.group(5));
            wrapper.setSize(matcher.group(6));
            return wrapper;
        }
        return null;
    }
}
