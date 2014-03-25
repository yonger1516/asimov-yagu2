package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.FirewallPolicyMgmtDataRequestWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirewallPolicyMgmtDataRequestTask extends Task<FirewallPolicyMgmtDataRequestWrapper> {
    private static final String FIREWALL_POLICY_MGMT_DATA_REQUEST_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Sending a firewall policy mgmt data request.*";
    private static final Pattern firewallPolicyMgmtDataRequestPattern = Pattern.compile(FIREWALL_POLICY_MGMT_DATA_REQUEST_REGEXP, Pattern.CASE_INSENSITIVE);

    protected FirewallPolicyMgmtDataRequestWrapper parseLine(String line) {
        Matcher matcher = firewallPolicyMgmtDataRequestPattern.matcher(line);
        if (matcher.find()) {
            FirewallPolicyMgmtDataRequestWrapper wrapper = new FirewallPolicyMgmtDataRequestWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s="07-04 14:50:08.921 D/Asimov::Java::AbstractZ7TransportMultiplexer( 1745): 2013/07/04 14:50:08.926000 EEST 163 [DEBUG] [com.seven.transport.AbstractZ7TransportMultiplexer] ---> Sending a firewall policy mgmt data request [0-860-0->0-13-0:4] of 80 bytes to endpoint 0-13-0. A delivery observer is observing this message (token: 502). The message has a relay id hint 0\n";
        FirewallPolicyMgmtDataRequestTask task = new FirewallPolicyMgmtDataRequestTask();
        FirewallPolicyMgmtDataRequestWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
    }
}
