package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.ReportTransferWrapperNN;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportTransferTaskNN extends Task<ReportTransferWrapperNN> {

    private static final String TAG = ReportTransferTaskNN.class.getSimpleName();

    private static final String REPORT_TRANSFER_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*status=([A-Z_]*).*type=([A-Z_]*).*token=([0-9]*).*changed status from: ([A-Z_]*) to:([A-Z_]*)";;

    private static final Pattern reportTransferPattern = Pattern.compile(REPORT_TRANSFER_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public ReportTransferWrapperNN parseLine(String line) {
        Matcher matcher = reportTransferPattern.matcher(line);
        if (matcher.find()) {
            ReportTransferWrapperNN wrapper = new ReportTransferWrapperNN();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setStatus(matcher.group(3));
            wrapper.setType(matcher.group(4));
            wrapper.setToken(matcher.group(5));
            wrapper.setFromStatus(matcher.group(6));
            wrapper.setToStatus(matcher.group(7));
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
