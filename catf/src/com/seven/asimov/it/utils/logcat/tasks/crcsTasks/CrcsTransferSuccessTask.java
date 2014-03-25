package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CrcsTransferSuccessWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrcsTransferSuccessTask extends Task<CrcsTransferSuccessWrapper> {

    private static final String TAG = CrcsTransferSuccessTask.class.getSimpleName();

    private static final String CRCS_TRANSFER_SUCCESS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*CRCS logs transfer successful";

    private static final Pattern crcsTransferSuccessPattern = Pattern.compile(CRCS_TRANSFER_SUCCESS_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public CrcsTransferSuccessWrapper parseLine(String line) {
        Matcher matcher = crcsTransferSuccessPattern.matcher(line);
        if (matcher.find()) {
            CrcsTransferSuccessWrapper wrapper = new CrcsTransferSuccessWrapper();
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
