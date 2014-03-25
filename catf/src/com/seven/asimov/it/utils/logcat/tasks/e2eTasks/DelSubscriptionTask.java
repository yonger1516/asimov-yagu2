package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DelSubscriptionWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DelSubscriptionTask extends Task<DelSubscriptionWrapper> {

    private static final String TAG = DelSubscriptionTask.class.getSimpleName();

    private static final String DELETE_SUBSCRIPTION_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Subscription.*\\[([0-9]*).*RR.*\\[([0-9]*).*status DELETED";
    private static final Pattern deleteSubscriptionPattern = Pattern.compile(DELETE_SUBSCRIPTION_REGEXP);

    @Override
    protected DelSubscriptionWrapper parseLine(String line) {
        Matcher matcher = deleteSubscriptionPattern.matcher(line);
        if (matcher.find()) {
            DelSubscriptionWrapper wrapper = new DelSubscriptionWrapper(0, matcher.group(3), matcher.group(4));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}


