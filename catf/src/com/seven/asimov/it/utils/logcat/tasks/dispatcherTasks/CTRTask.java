package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CTRWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CTRTask extends Task<CTRWrapper> {
    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+Resolver.cpp:[0-9]+.\\s\\([0-9]+\\).-.CSM \\[([0-9A-Z]+)\\] IN EndPoint Resolver uniq id \\[([0-9A-Z]+)\\] CTR received";
    private static Pattern ccrPattern = Pattern.compile(FCL_REGEXP);

    protected CTRWrapper parseLine(String line) {
        Matcher matcher = ccrPattern.matcher(line);
        if (matcher.find()) {
            CTRWrapper wrapper = new CTRWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setCsmId(matcher.group(3));
            wrapper.setResolverId(matcher.group(4));
            return wrapper;
        }
        return null;
    }
}
