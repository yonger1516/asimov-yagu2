package com.seven.asimov.it.utils.logcat.tasks.failoverTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FloIpcMessageWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FloIpcMessageTask extends Task<FloIpcMessageWrapper> {
    private static final String FLO_IPC_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*OC1: Sent ([A-Z]+) \\([a-z]+=(\\d) [a-z,\\S]+=(\\d)\\)";
    private static final Pattern floIpcPattern = Pattern.compile(FLO_IPC_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected FloIpcMessageWrapper parseLine(String line) {
        Matcher matcher = floIpcPattern.matcher(line);
        if (matcher.find()) {
            FloIpcMessageWrapper wrapper = new FloIpcMessageWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setMessage(matcher.group(3));
            if (matcher.group(3).equals("FLO")) {
                wrapper.setActionFLO(matcher.group(4));
                wrapper.setOptionsFLO(matcher.group(5));
            } else {
                wrapper.setTypeIPC(matcher.group(4));
                wrapper.setChainIdIPC(matcher.group(5));
            }
            return wrapper;
        }
        return null;
    }
}
