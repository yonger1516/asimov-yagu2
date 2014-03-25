package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.ThWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThTask extends Task<ThWrapper> {
    private static final String TAG = ThTask.class.getSimpleName();

    private static final String TH_REGEXP = "([A-Z]?)/Asimov::Java::SubscriptionService\\(\\s?[0-9]+\\): (201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*ThNotification \\[clientSubscriptionId=([0-9]*).*type=([0-9]*)";
    private static final Pattern ThPattern = Pattern.compile(TH_REGEXP, Pattern.CASE_INSENSITIVE);


    protected ThWrapper parseLine(String line) {
        Matcher matcher = ThPattern.matcher(line);
        if (matcher.find()) {
            ThWrapper wrapper = new ThWrapper();
            wrapper.setLogLevel(matcher.group(1));
            setTimestampToWrapper(wrapper, matcher, 2, 3);
            wrapper.setId(Integer.parseInt(matcher.group(4)));
            wrapper.setType(Byte.parseByte(matcher.group(5)));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
