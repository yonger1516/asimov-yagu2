package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.SubscriptionIdNotFoundMsg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubscriptionIdNotFoundMsgTask extends Task<SubscriptionIdNotFoundMsg> {
    private static final String SUBSCR_NOT_FOUND_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*\\[WARNING\\].*subscription id is not found";
    private static final Pattern subscrNotFoundPattern = Pattern.compile(SUBSCR_NOT_FOUND_REGEXP);

    @Override
    protected SubscriptionIdNotFoundMsg parseLine(String line) {
        Matcher matcher = subscrNotFoundPattern.matcher(line);
        if (matcher.find()) {
            SubscriptionIdNotFoundMsg wrapper = new SubscriptionIdNotFoundMsg();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
