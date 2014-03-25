package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.tasks.policyTasks.PolicyAddedTask;
import com.seven.asimov.it.utils.logcat.wrappers.SubscriptionStatusWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SubscriptionStatusTask extends Task<SubscriptionStatusWrapper> {
    private static final String TAG = PolicyAddedTask.class.getSimpleName();

    private String SUBSCRIPTION_STATUS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Subscription \\[(%s)\\] RR \\[(%s)\\]: status (%s).*";
    private static final String subscriptionIDRegex = "-?\\d+";
    private static final String RRRegex = ".*";
    private static final String StatusRegex = "[A-Z_]*";
    private Pattern subscriptionStatusPattern;

    public SubscriptionStatusTask(String subscriptionID, String RR, String Status) {
        configureTask(subscriptionID, RR, Status);
    }

    public void configureTask(String subscriptionID, String RR, String Status) {
        SUBSCRIPTION_STATUS_REGEXP = String.format(SUBSCRIPTION_STATUS_REGEXP,
                subscriptionID == null ? subscriptionIDRegex : subscriptionID,
                RR == null ? RRRegex : RR,
                Status == null ? StatusRegex : Status);
        subscriptionStatusPattern = Pattern.compile(SUBSCRIPTION_STATUS_REGEXP, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public SubscriptionStatusWrapper parseLine(String line) {
        Matcher matcher = subscriptionStatusPattern.matcher(line);
        if (matcher.find()) {
            SubscriptionStatusWrapper wrapper = new SubscriptionStatusWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setSubscriptionID(matcher.group(3));
            wrapper.setRR(matcher.group(4));
            wrapper.setStatus(matcher.group(5));
            return wrapper;
        }

        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
