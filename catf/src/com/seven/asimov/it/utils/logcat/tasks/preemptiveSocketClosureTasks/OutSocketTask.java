package com.seven.asimov.it.utils.logcat.tasks.preemptiveSocketClosureTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.OutSoketWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutSocketTask extends Task<OutSoketWrapper> {
    private static final String TAG = OutSocketTask.class.getSimpleName();
    public static final String CLOSING_ALL_OUT_CONNECTIONS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*Closing all OUT connections";
    public static final String DETECTION_INTERVAL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*entered Detection Interval for %s seconds";
    public static final String COOLDOWN_INTERVAL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*entered into CoolDown Interval for %s seconds";
    public static final String COOLDOWN_INTERVAL_IN_ACTION_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*CoolDown Interval in action";

    private Pattern outSocketPattern;

    public OutSocketTask(String outSocketRegexpName) {
        Log.v(TAG, "OUT Socket Regexp = " + outSocketRegexpName);
        outSocketPattern = Pattern.compile(outSocketRegexpName, Pattern.CASE_INSENSITIVE);
    }

    public OutSocketTask(String outSocketRegexpName, int interval) {
        outSocketRegexpName = String.format(outSocketRegexpName, interval);
        Log.v(TAG, "OUT Socket Regexp = " + outSocketRegexpName);
        outSocketPattern = Pattern.compile(outSocketRegexpName, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected OutSoketWrapper parseLine(String line) {
        Matcher matcher = outSocketPattern.matcher(line);
        if(matcher.find()){
            OutSoketWrapper wrapper = new OutSoketWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
