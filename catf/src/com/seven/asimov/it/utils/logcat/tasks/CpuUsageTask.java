package com.seven.asimov.it.utils.logcat.tasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.CpuUsageWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CpuUsageTask extends Task<CpuUsageWrapper> {
    private static final String TAG = CpuUsageTask.class.getSimpleName();
    private String cpuUsageRegexp =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*\\[%s\\].*%s CPU usage \\(([0-9]*).[0-9]*\\S on device with . cores\\) %s";
    private Pattern cpuUsagePattern;

    private String logLevel;
    private String cpuUsageOf;
    private String messageLevel;

    public CpuUsageTask(String logLevel, String cpuUsageOf, String messageLevel) {
        this.logLevel = logLevel;
        this.cpuUsageOf = cpuUsageOf;
        this.messageLevel = messageLevel;

        cpuUsageRegexp = String.format(cpuUsageRegexp, logLevel, cpuUsageOf, messageLevel);
        Log.v(TAG, "cpuUsageRegexp=" + cpuUsageRegexp);
        cpuUsagePattern = Pattern.compile(cpuUsageRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected CpuUsageWrapper parseLine(String line) {
        Matcher matcher = cpuUsagePattern.matcher(line);
        if(matcher.find()){
            CpuUsageWrapper cpuUsageWrapper = new CpuUsageWrapper();
            setTimestampToWrapper(cpuUsageWrapper, matcher);
            cpuUsageWrapper.setLogLevel(logLevel);
            cpuUsageWrapper.setCpuUsageOf(cpuUsageOf);
            cpuUsageWrapper.setMessageLevel(messageLevel);
            cpuUsageWrapper.setPercentUsage(Integer.parseInt(matcher.group(3)));
            return cpuUsageWrapper;
        }
        return null;
    }
}
