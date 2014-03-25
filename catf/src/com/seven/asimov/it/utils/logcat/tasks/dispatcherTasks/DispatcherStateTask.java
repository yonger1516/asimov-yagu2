package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DispatcherStateWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherStateTask extends Task<DispatcherStateWrapper> {

    private static final String TAG = DispatcherStateTask.class.getSimpleName();
    private final static String DISPATCHER_REGEXP = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).* - " +
            "SSM\\s([A-Z]+)\\s\\(id=([0-9]+),.pid=([0-9]+),.state=([A-Z]+)\\)";
    private final static Pattern dispatcherPattern = Pattern.compile(DISPATCHER_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected DispatcherStateWrapper parseLine(String line) {
        Matcher matcher = dispatcherPattern.matcher(line);
        if(matcher.find()){
            DispatcherStateWrapper wrapper = new DispatcherStateWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setName(matcher.group(3));
            wrapper.setId(Long.parseLong(matcher.group(4)));
            wrapper.setPid(Integer.parseInt(matcher.group(5)));
            wrapper.setState(matcher.group(6));
            return wrapper;
        }
        return null;
    }
}
