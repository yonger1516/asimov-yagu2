package com.seven.asimov.it.utils.logcat.tasks.firewallTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ResponseFirewallPolicyReceivedWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseFirewallPolicyReceivedTask extends Task<ResponseFirewallPolicyReceivedWrapper> {
    private static final String RESPONSE_FIREWALL_POLICY_RECEIVED_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Response.*firewall policy.*received.*status.*Error\\(code=([0-9a-zA-Z]*), result=([0-9a-zA-Z]*).*";
    private static final Pattern responseFirewallPolicyReceivedPattern = Pattern.compile(RESPONSE_FIREWALL_POLICY_RECEIVED_REGEXP, Pattern.CASE_INSENSITIVE);

    protected ResponseFirewallPolicyReceivedWrapper parseLine(String line) {
        Matcher matcher = responseFirewallPolicyReceivedPattern.matcher(line);
        if (matcher.find()) {
            ResponseFirewallPolicyReceivedWrapper wrapper = new ResponseFirewallPolicyReceivedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setErrorCode(matcher.group(3));
            wrapper.setResult(matcher.group(4));
            return wrapper;
        }
        return null;
    }
      public static void main(String[] args) {
        String s = "07-04 14:50:09.304 I/Asimov::Java::FirewallPolicyServiceImpl( 1745): 2013/07/04 14:50:09.307000 EEST 165 [INFO] [com.seven.pcf.service.FirewallPolicyServiceImpl] : Response 'firewall policy' received. status: Error(code=6015, result=c0010004)\n";
        ResponseFirewallPolicyReceivedTask task = new ResponseFirewallPolicyReceivedTask();
        ResponseFirewallPolicyReceivedWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
    }
}
