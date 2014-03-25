package com.seven.asimov.it.utils.logcat.tasks.firewallTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.StartingFirewallWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StartingFirewallTask extends Task<StartingFirewallWrapper> {
    private static final String STARTING_FIREWALL_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Starting Firewall.*";
    private static final Pattern startingFirewallPattern = Pattern.compile(STARTING_FIREWALL_REGEXP, Pattern.CASE_INSENSITIVE);

    protected StartingFirewallWrapper parseLine(String line) {
        Matcher matcher = startingFirewallPattern.matcher(line);
        if (matcher.find()) {
            StartingFirewallWrapper wrapper = new StartingFirewallWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s = "07-04 14:50:09.078 V/Asimov::JNI::OCEngine( 1745): 2013/07/04 14:50:09.083989 EEST 2385 [FTRACE]\t[pcf_firewall.cpp:190] (0) - Starting Firewall";
        StartingFirewallTask task = new StartingFirewallTask();
        StartingFirewallWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
    }
}