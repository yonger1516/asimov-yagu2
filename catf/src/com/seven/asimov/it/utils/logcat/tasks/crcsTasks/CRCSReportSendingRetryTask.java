package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seven.asimov.it.utils.logcat.wrappers.CRCSReportSendingRetryWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;

public class CRCSReportSendingRetryTask extends Task<CRCSReportSendingRetryWrapper> {

    private static final String TAG = CRCSReportSendingRetryTask.class.getSimpleName();

    private static String CRCS_REPORT_SENDING_RETRY_TASK = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* Scheduling reconnect in ([0-9])*ms";

    private static final Pattern crcsSendingReportRetryPattern = Pattern.compile(CRCS_REPORT_SENDING_RETRY_TASK, Pattern.CASE_INSENSITIVE);

    @Override
    protected CRCSReportSendingRetryWrapper parseLine(String line) {

        Matcher matcher = crcsSendingReportRetryPattern.matcher(line);
        if (matcher.find()) {
            CRCSReportSendingRetryWrapper wrapper = new CRCSReportSendingRetryWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
