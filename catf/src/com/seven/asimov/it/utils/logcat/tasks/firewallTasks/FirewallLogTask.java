package com.seven.asimov.it.utils.logcat.tasks.firewallTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.conditionTasks.ScriptLogTask;
import com.seven.asimov.it.utils.logcat.wrappers.FirewallLogWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirewallLogTask extends Task<FirewallLogWrapper> {
    private static final String TAG = ScriptLogTask.class.getSimpleName();

    private static final String FL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*FirewallLog: (.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*),(.*)";
    private static Pattern FirewallLogPattern = Pattern.compile(FL_REGEXP, Pattern.CASE_INSENSITIVE);


    @Override
    protected FirewallLogWrapper parseLine(String line) {
        Matcher matcher = FirewallLogPattern.matcher(line);

        if (matcher.find()) {
            FirewallLogWrapper wrapper = new FirewallLogWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setFirewall(matcher.group(4));
            wrapper.setVersion(matcher.group(5));
            wrapper.setType(matcher.group(6));
            wrapper.setChainID(Long.parseLong(matcher.group(7)));
            wrapper.setName(matcher.group(8));
            wrapper.setAction(matcher.group(9));
            wrapper.setEvent(matcher.group(10));
            wrapper.setNetworkIface(matcher.group(11));
            wrapper.setIPVersion(matcher.group(12));
            wrapper.setSequenceNumber(Long.parseLong(matcher.group(13)));
            return wrapper;

        }
        return null;
    }
}
