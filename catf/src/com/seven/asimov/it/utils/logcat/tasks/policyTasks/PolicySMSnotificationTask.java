package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicySMSnotificationWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicySMSnotificationTask extends Task<PolicySMSnotificationWrapper> {
    private static final String TAG = PolicySMSnotificationWrapper.class.getSimpleName();

    private static final String POLICY_SMS_NOTIFICATION_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*nf: SmsNotification .triggerCommandId=([0-9]*), addressInfo=([0-9]*), appData=(.*), origPacketId=([0-9]*), payloadLength=([0-9]*).";
    private static final Pattern policySMSnotificationPattern = Pattern.compile(POLICY_SMS_NOTIFICATION_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected PolicySMSnotificationWrapper parseLine(String line) {
        Matcher matcher = policySMSnotificationPattern.matcher(line);
        if (matcher.find()) {
            PolicySMSnotificationWrapper wrapper = new PolicySMSnotificationWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setTriggerCommandId(matcher.group(3));
            wrapper.setAddressInfo(matcher.group(4));
            wrapper.setAppData(matcher.group(5));
            wrapper.setOrigPacketId(matcher.group(6));
            wrapper.setPayloadLength(matcher.group(7));
            return wrapper;
        }
        return null;
    }
}
