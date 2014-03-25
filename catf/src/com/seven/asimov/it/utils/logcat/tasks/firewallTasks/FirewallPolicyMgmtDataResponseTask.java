package com.seven.asimov.it.utils.logcat.tasks.firewallTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FirewallPolicyMgmtDataResponseWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirewallPolicyMgmtDataResponseTask extends Task<FirewallPolicyMgmtDataResponseWrapper> {
    private static final String FIREWALL_POLICY_MGMT_DATA_RESPONSE_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Received a firewall policy mgmt server response.*";
    private static final Pattern firewallPolicyMgmtDataResponsePattern = Pattern.compile(FIREWALL_POLICY_MGMT_DATA_RESPONSE_REGEXP, Pattern.CASE_INSENSITIVE);

    protected FirewallPolicyMgmtDataResponseWrapper parseLine(String line) {
        Matcher matcher = firewallPolicyMgmtDataResponsePattern.matcher(line);
        if (matcher.find()) {
            FirewallPolicyMgmtDataResponseWrapper wrapper = new FirewallPolicyMgmtDataResponseWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s = "07-04 14:50:09.281 D/Asimov::Java::AbstractZ7TransportMultiplexer( 1745): 2013/07/04 14:50:09.285000 EEST 165 [DEBUG] [com.seven.transport.AbstractZ7TransportMultiplexer] <--- Received a firewall policy mgmt server response [0-13-2->0-860-0:4] of 96 bytes from endpoint 0-13-2. The message has a relay id hint 2\n";
        FirewallPolicyMgmtDataResponseTask task = new FirewallPolicyMgmtDataResponseTask();
        FirewallPolicyMgmtDataResponseWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
    }
}

