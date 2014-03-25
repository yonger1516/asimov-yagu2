package com.seven.asimov.it.utils.logcat.tasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.ServiceLogWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceLogTask extends Task<ServiceLogWrapper> {
    private static final String TAG = CpuUsageTask.class.getSimpleName();

    private String serviceLogRegexp =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*ServiceLog: [0-9,\\s,\\.\\-,:]*service,[0-9]*,(%s),(%s),(%s),(%s),[0-9]*";

    private Pattern serviceLogPattern;

    public ServiceLogTask(String cpuUsageOf, String cpuColor) {
        this("service","event",cpuUsageOf,cpuColor);
    }


    public ServiceLogTask(String type, String action,String extra1,String extra2) {
        serviceLogRegexp = String.format(serviceLogRegexp, type==null?".*":type, action==null?".*":action,extra1==null?".*":extra1,extra2==null?".*":extra2);
        Log.v(TAG, "serviceLogRegexp=" + serviceLogRegexp);
        serviceLogPattern = Pattern.compile(serviceLogRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected ServiceLogWrapper parseLine(String line) {
        Matcher matcher = serviceLogPattern.matcher(line);
        if(matcher.find()){
            ServiceLogWrapper serviceLogWrapper = new ServiceLogWrapper();
            setTimestampToWrapper(serviceLogWrapper, matcher);
            serviceLogWrapper.setType(matcher.group(3));
            serviceLogWrapper.setAction(matcher.group(4));
            serviceLogWrapper.setExtra1(matcher.group(5));
            serviceLogWrapper.setExtra2(matcher.group(6));
            return serviceLogWrapper;
        }
        return null;
    }
}
