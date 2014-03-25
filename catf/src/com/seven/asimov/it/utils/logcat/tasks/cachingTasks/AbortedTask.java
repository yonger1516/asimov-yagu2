package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.AbortedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AbortedTask extends Task<AbortedWrapper> {

    private static final String TAG = AbortedTask.class.getSimpleName();

//    private static final String ABRT_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*HTRX \\[([0-9A-Z]*).*verdict ABRT";
    private static final String ABRT_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*OC.*: Sent ABRT for originator .([0-9A-Z]*).";
    private static final Pattern ABRT_PATTERN = Pattern.compile(ABRT_REGEXP,Pattern.CASE_INSENSITIVE);


    protected AbortedWrapper parseLine(String line){
        Matcher matcher1 = ABRT_PATTERN.matcher(line);
        if (matcher1.find()){
            AbortedWrapper wrapper = new AbortedWrapper();
            setTimestampToWrapper(wrapper,matcher1);
            wrapper.setHtrx(matcher1.group(3));
            return wrapper;
        }
        return null;
    }
}