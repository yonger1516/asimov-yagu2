package com.seven.asimov.it.utils.logcat.tasks.msisdnTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnValidationHttpRequestWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsisdnValidationHttpRequestTask extends Task<MsisdnValidationHttpRequestWrapper> {
    private static final String TAG = MsisdnValidationHttpRequestTask.class.getSimpleName();

    private static final String MSISDN_HTTP_REQUEST_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*MSISDN validation request to: (http://[\\w./?]*content=)";
    private static final Pattern msisdnHttpRequestPattern = Pattern.compile(MSISDN_HTTP_REQUEST_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public MsisdnValidationHttpRequestWrapper parseLine(String line) {
        Matcher matcher = msisdnHttpRequestPattern.matcher(line);
        if (matcher.find()) {
            MsisdnValidationHttpRequestWrapper wrapper = new MsisdnValidationHttpRequestWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setUrl(matcher.group(3));
            //Log.d(TAG, "parseLine found line " + matcher.group(1));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}

