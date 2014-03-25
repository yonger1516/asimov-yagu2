package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyMGMTUpdateWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyMGMTUpdateTask extends Task<PolicyMGMTUpdateWrapper> {
    private static final String TAG = PolicyMGMTUpdateTask.class.getSimpleName();

    private static final String POLICY_MGMT_UPDATE_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*<--- Received a policy mgmt update notification .*:([0-9].*). of.*hint.[0-9]*";
    private static final Pattern policyMGMTUpdatePattern = Pattern.compile(POLICY_MGMT_UPDATE_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected PolicyMGMTUpdateWrapper parseLine(String line) {
        Matcher matcher = policyMGMTUpdatePattern.matcher(line);
        if (matcher.find()) {
            PolicyMGMTUpdateWrapper wrapper = new PolicyMGMTUpdateWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setPacketID(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
