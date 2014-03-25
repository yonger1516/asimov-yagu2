package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ChainManagerConstructorWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChainManagerConstructorTask extends Task<ChainManagerConstructorWrapper> {
    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.+ChainManager.hpp:[0-9]+.\\s\\([0-9]+\\).-.ChainManager \\((.+)\\) constructor.";
    private static Pattern ccrPattern = Pattern.compile(FCL_REGEXP);

    protected ChainManagerConstructorWrapper parseLine(String line) {
        Matcher matcher = ccrPattern.matcher(line);
        if (matcher.find()) {
            ChainManagerConstructorWrapper wrapper = new ChainManagerConstructorWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setChainManagerId(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
