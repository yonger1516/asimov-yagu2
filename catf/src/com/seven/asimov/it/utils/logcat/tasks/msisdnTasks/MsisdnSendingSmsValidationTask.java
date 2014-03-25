package com.seven.asimov.it.utils.logcat.tasks.msisdnTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnSendingSmsValidationWrapper;

public class MsisdnSendingSmsValidationTask extends Task<MsisdnSendingSmsValidationWrapper> {
    private static final String TAG = MsisdnSendingSmsValidationTask.class.getSimpleName();

    private static final String MSISDN_SEND_SMS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Sending SMS validation message to.*(\\+[0-9]*).*message: ([\\w ]*)";
    private static final Pattern msisdnSendSmsPattern = Pattern.compile(MSISDN_SEND_SMS_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public MsisdnSendingSmsValidationWrapper parseLine(String line) {
        Matcher matcher = msisdnSendSmsPattern.matcher(line);
        if (matcher.find()) {
            MsisdnSendingSmsValidationWrapper wrapper = new MsisdnSendingSmsValidationWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setPhoneToSend(matcher.group(3));
            wrapper.setMessage(matcher.group(4));
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
