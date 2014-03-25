package com.seven.asimov.it.utils.logcat.tasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.LogLabelWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 11/21/13
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 *
 * This task is created to handle custom log messages and spot some key points during the test.
 * To use it create task instance with tag and label (some message) that will be used in log record.
 *
 */
public class LogLabelTask  extends Task<LogLabelWrapper> {
    private static final String TAG = LogLabelTask.class.getSimpleName();
    private String logLabelRegexp =  ".*%s\\(\\ *[0-9]*\\).*%s";
    private Pattern logLabelPattern;

    private String tag;
    private String label;

    public LogLabelTask(String tag, String label) {
        this.tag = tag;
        this.label = label;

        logLabelRegexp = String.format(logLabelRegexp, tag, label);
        Log.v(TAG, "logLabelRegexp=" + logLabelRegexp);
        logLabelPattern = Pattern.compile(logLabelRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public LogLabelWrapper parseLine(String line) {
        Matcher matcher = logLabelPattern.matcher(line);
        if (matcher.find()) {
            LogLabelWrapper wrapper = new LogLabelWrapper();
            //Log.v(TAG, "parseLine: found line");
            wrapper.setTimestamp(System.currentTimeMillis());
            wrapper.setTag(tag);
            wrapper.setLabel(label);
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

}
