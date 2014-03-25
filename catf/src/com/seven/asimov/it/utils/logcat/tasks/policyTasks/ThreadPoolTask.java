package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ThreadPoolWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThreadPoolTask extends Task<ThreadPoolWrapper> {
    private static final String THREAD_POOL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*(\\w+).*Scheduled tasks \\(active \\d, pending ([\\d]+)\\)";
    private final Pattern pattern = Pattern.compile(THREAD_POOL_REGEXP);

    @Override
    protected ThreadPoolWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ThreadPoolWrapper threadPoolWrapper = new ThreadPoolWrapper(matcher.group(4));
            setTimestampToWrapper(threadPoolWrapper, matcher);
            return threadPoolWrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s2 = "08-16 15:32:55.145 D/Asimov::JNI::OCEngine( 3507): 2013/08/16 15:32:55.149646 EEST 4953 [DEBUG]\t[threadpool.cpp:203] (0) - Scheduled tasks (active 8, pending 1):";

        ThreadPoolTask task = new ThreadPoolTask();
        ThreadPoolWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}