package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.GetCachedResponseWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetCachedResponseTask extends Task<GetCachedResponseWrapper> {
    private static final String TAG = GetCachedResponseTask.class.getSimpleName();

    private String GET_CACHED_RESPONSE_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Sending a traffic harmonizer get cached response request.*subscriptionId=(%s),";
    private final String subscriptionIdRegex = "-?\\d+";
    private Pattern getCachedResponsePattern;

    public GetCachedResponseTask(String subscriptionId) {
        configureTask(subscriptionId);
    }

    public void configureTask(String subscriptionId) {
        GET_CACHED_RESPONSE_REGEXP = String.format(GET_CACHED_RESPONSE_REGEXP, subscriptionId == null ? subscriptionIdRegex : subscriptionId);
        getCachedResponsePattern = Pattern.compile(GET_CACHED_RESPONSE_REGEXP, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected GetCachedResponseWrapper parseLine(String line) {
        Matcher matcher = getCachedResponsePattern.matcher(line);
        if (matcher.find()) {
            GetCachedResponseWrapper wrapper = new GetCachedResponseWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setSubscriptionId(Integer.parseInt(matcher.group(3)));

            return wrapper;
        }
        return null;
    }
}
