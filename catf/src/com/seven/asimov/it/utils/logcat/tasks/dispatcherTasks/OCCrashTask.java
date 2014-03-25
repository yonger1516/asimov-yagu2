package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.OCCrashWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCCrashTask extends Task<OCCrashWrapper> {
    private static final String OC_CRASH_REGEXP =
            "([0-9]*-[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*(\\w+).*pid: (\\d*), tid: (\\d*), name: (\\w*)\\s";
    private final Pattern pattern = Pattern.compile(OC_CRASH_REGEXP);

    @Override
    protected OCCrashWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            OCCrashWrapper ocEngineCrashWrapper = new OCCrashWrapper(matcher.group(4), matcher.group(5), matcher.group(6));
            return ocEngineCrashWrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s2 = "08-21 17:09:08.446 I/DEBUG   (  156): pid: 2575, tid: 3805, name: OCEngineService  >>> com.seven.asimov <<<";
        OCCrashTask task = new OCCrashTask();
        OCCrashWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}