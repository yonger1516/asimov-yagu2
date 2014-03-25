package com.seven.asimov.it.utils.logcat.tasks.firewallTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FirewallDisabledWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirewallDisabledTask extends Task<FirewallDisabledWrapper> {
    private static final String FIREWALL_DISABLED_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Firewall is disabled";
    private static final Pattern firewallDisabledPattern = Pattern.compile(FIREWALL_DISABLED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected FirewallDisabledWrapper parseLine(String line) {
        Matcher matcher = firewallDisabledPattern.matcher(line);
        if (matcher.find()) {
            FirewallDisabledWrapper wrapper = new FirewallDisabledWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
