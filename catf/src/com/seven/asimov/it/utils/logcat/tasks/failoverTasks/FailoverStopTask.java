package com.seven.asimov.it.utils.logcat.tasks.failoverTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FailoverStopWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FailoverStopTask extends Task<FailoverStopWrapper> {

    private String failoverActive = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*- (.*) failover has been stopped";
    Pattern patternFailoverActive = Pattern.compile(failoverActive);

    protected FailoverStopWrapper parseLine(String line) {
        Matcher matcherFailoverActive = patternFailoverActive.matcher(line);
        if (matcherFailoverActive.find()) {
            FailoverStopWrapper failoverWrapper = new FailoverStopWrapper();
            setTimestampToWrapper(failoverWrapper, matcherFailoverActive);
            failoverWrapper.setFailoverType(matcherFailoverActive.group(3));
            return failoverWrapper;
        }
        return null;
    }
}