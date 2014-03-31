package com.seven.asimov.it.utils.logcat.tasks.msisdnTasks;

import android.util.Log;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.MsisdnWrapper;
import com.seven.asimov.it.utils.logcat.wrappers.TimeZones;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MsisdnTask extends Task<MsisdnWrapper> {

    private static final String TAG = MsisdnWrapper.class.getSimpleName();


    private static final String MSISDN_SUCCESS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*MSISDN validation success. updating the validation trigger";
    private static final Pattern msisdnSuccessPattern = Pattern.compile(MSISDN_SUCCESS_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String IMSI_DETECTED_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*imsi:.([0-9]*).*lastImsi";
    private static final Pattern imsiDetectedPattern = Pattern.compile(IMSI_DETECTED_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String VALIDATION_CHECKING_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*MSISDN Validation is enabled and not done yet - so it is required";
    private static final Pattern validationNeededCheckingPattern = Pattern.compile(VALIDATION_CHECKING_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String MSISDN_VALUE_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*getStored: MSISDN_VALIDATION_MSISDN = ([0-9]*)";

    private static final Pattern msisdnValuePattern = Pattern.compile(MSISDN_VALUE_REGEXP, Pattern.CASE_INSENSITIVE);


    private static MsisdnWrapper wrapper = null;

    @Override
    public MsisdnWrapper parseLine(String line) {

        Matcher matcher1 = msisdnSuccessPattern.matcher(line);
        Matcher matcher2 = imsiDetectedPattern.matcher(line);
        Matcher matcher3 = validationNeededCheckingPattern.matcher(line);
        Matcher matcher4 = msisdnValuePattern.matcher(line);

        if (matcher1.find()) {

            setTimestampToWrapper(getWrapper(),matcher1,1,2);
            //getWrapper().setTimestamp(getTimestamp(matcher1.group(1), matcher1.group(2)));
            getWrapper().setMsisdnSuccess(true);
            return wrapper;
        }
        if (matcher2.find()) {
            setTimestampToWrapper(getWrapper(),matcher1,1,2);
            //getWrapper().setTimestamp(getTimestamp(matcher2.group(1), matcher2.group(2)));
            getWrapper().setImsi(Long.parseLong(matcher2.group(3)));
            return wrapper;
        }
        if (matcher3.find()) {
            setTimestampToWrapper(getWrapper(),matcher1,1,2);
            //getWrapper().setTimestamp(getTimestamp(matcher3.group(1), matcher3.group(2)));
            getWrapper().setValidationNeeded(true);
            return wrapper;
        }
        if (matcher4.find()) {
            setTimestampToWrapper(getWrapper(),matcher1,1,2);
            //getWrapper().setTimestamp(getTimestamp(matcher4.group(1), matcher4.group(2)));
            getWrapper().setMsisdn(matcher4.group(3));
        }
        return null;
    }

    private long getTimestamp(String groupTimestamp, String groupTimezone) {
        int hour = 0;
        try {
            hour = TimeZones.valueOf(groupTimezone).getId();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }
        long timestamp = DateUtil.format(groupTimestamp.replaceAll("/", "-")) + hour * 3600 * 1000;
        return timestamp;
    }

    private MsisdnWrapper getWrapper() {
        if (wrapper == null &&
                getLogEntries().size() == 0) {
            wrapper = new MsisdnWrapper();
        }
        return wrapper;
    }

    public void cleanWrapper() {
        wrapper = null;
    }
}
