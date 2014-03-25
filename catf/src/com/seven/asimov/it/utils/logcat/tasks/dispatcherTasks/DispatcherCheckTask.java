package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DispatcherCheckWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherCheckTask extends Task <DispatcherCheckWrapper> {
    private static final String REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*\\s(SSM)\\s([^\\s]+)\\s.id=([0-9]+),.pid=([0-9]+),.port=([0-9]+),.state=([A-Z]+).\\sreceived CTQ";
    private static Pattern pattern = Pattern.compile(REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected DispatcherCheckWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            DispatcherCheckWrapper wrapper = new DispatcherCheckWrapper();

            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDispatcherName(matcher.group(4));
            wrapper.setDispatcherId(matcher.group(5));
            wrapper.setDispatcherPid(matcher.group(6));
            wrapper.setDispatcherPort(matcher.group(7));
            wrapper.setDispatcherStatus(matcher.group(8));


            return wrapper;
        }
        return null;
    }
}
