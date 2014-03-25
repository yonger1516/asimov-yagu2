package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.StandaloneRmpExpirationMsg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StandaloneRmpExpirationMsgTask extends Task<StandaloneRmpExpirationMsg> {
    private static final String RMP_EXPIRED_REGEXP =
            "(201[2-9].[0-1][0-9].[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*).*\\[TRACE\\].*standalone RMP expired";
    private static final Pattern rmpExpiredPattern = Pattern.compile(RMP_EXPIRED_REGEXP);

    @Override
    protected StandaloneRmpExpirationMsg parseLine(String line) {
        Matcher matcher = rmpExpiredPattern.matcher(line);
        if (matcher.find()) {
            StandaloneRmpExpirationMsg wrapper = new StandaloneRmpExpirationMsg();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
