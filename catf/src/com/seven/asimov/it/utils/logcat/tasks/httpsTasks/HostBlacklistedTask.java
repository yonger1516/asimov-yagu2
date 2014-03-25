package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.HostBlacklistedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostBlacklistedTask extends Task<HostBlacklistedWrapper> {
    private final String HOST_BLACKLISTED = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Host.*blacklisted till (.*)";
    private Pattern hostBlacklistedPattern = Pattern.compile(HOST_BLACKLISTED, Pattern.CASE_INSENSITIVE);

    @Override
    protected HostBlacklistedWrapper parseLine(String line) {
        Matcher matcher = hostBlacklistedPattern.matcher(line);
        if (matcher.find()) {
            HostBlacklistedWrapper wrapper = new HostBlacklistedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setTill(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
