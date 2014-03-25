package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DispatchersConfigurationInvalidWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatcherConfigurationInvalidTask extends Task<DispatchersConfigurationInvalidWrapper> {

    private static final String POLICY_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).* Wrong dispatcher's configuration, dispatcher ([a-z]*), protocol ([0-9]+), ports (.*), z-order ([0-9]*)";
    private static final String INVALID_POLICY = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*:[0-9]*:[0-9]*\\.[0-9]*).([A-Z]*).* New dispatchers' configuration is invalid";
    private static final Pattern policyPattern = Pattern.compile(POLICY_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern policyInvPattern = Pattern.compile(INVALID_POLICY, Pattern.CASE_INSENSITIVE);

    @Override
    protected DispatchersConfigurationInvalidWrapper parseLine(String line) {
        Matcher matcher = policyPattern.matcher(line);
        Matcher matcher2 = policyInvPattern.matcher(line);
        if (matcher.find()) {
            DispatchersConfigurationInvalidWrapper wrapper = new DispatchersConfigurationInvalidWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDispatcherName(matcher.group(3));
            wrapper.setType(matcher.group(4));
            wrapper.setPorts(matcher.group(5));
            wrapper.setzOrder(matcher.group(6));
            return wrapper;
        }
        if (matcher2.find()) {
            DispatchersConfigurationInvalidWrapper wrapper = new DispatchersConfigurationInvalidWrapper();
            setTimestampToWrapper(wrapper, matcher2);
            wrapper.setInvalidMsgFound(true);
            return wrapper;
        }
        return null;
    }
}
