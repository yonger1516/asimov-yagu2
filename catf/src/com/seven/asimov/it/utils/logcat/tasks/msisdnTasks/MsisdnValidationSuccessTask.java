package com.seven.asimov.it.utils.logcat.tasks.msisdnTasks;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnValidationSuccessWrapper;

public class MsisdnValidationSuccessTask extends Task<MsisdnValidationSuccessWrapper> {
    private static final String TAG = MsisdnValidationSuccessTask.class.getSimpleName();

    private static final String MSISDN_SUCCESS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*MSISDN validation success. updating the validation trigger";
    private static final Pattern msisdnSuccessPattern = Pattern.compile(MSISDN_SUCCESS_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public MsisdnValidationSuccessWrapper parseLine(String line) {
        Matcher matcher = msisdnSuccessPattern.matcher(line);
        if (matcher.find()) {
            MsisdnValidationSuccessWrapper wrapper = new MsisdnValidationSuccessWrapper();
            setTimestampToWrapper(wrapper, matcher);
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
