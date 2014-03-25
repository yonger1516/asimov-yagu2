package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyReceivedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyReceivedTask extends Task<PolicyReceivedWrapper> {

    private static final String TAG = PolicyReceivedTask.class.getSimpleName();

    private static final String POLICY_RECEIVED_REGEXP = "([A-Z]?)/Asimov::Java::PolicyUtils.* (201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*com.seven.client.policy.PolicyUtils.*";
    private static final Pattern policyReceivedPattern = Pattern.compile(POLICY_RECEIVED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected PolicyReceivedWrapper parseLine(String line) {
        if (line.contains("addChild")){
            return null;
        }
        Matcher matcher = policyReceivedPattern.matcher(line);
        if (matcher.find()) {
            PolicyReceivedWrapper wrapper = new PolicyReceivedWrapper();
            wrapper.setLogLevel(matcher.group(1));
            setTimestampToWrapper(wrapper, matcher, 2, 3);
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
