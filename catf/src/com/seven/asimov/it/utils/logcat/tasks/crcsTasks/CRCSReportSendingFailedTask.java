package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seven.asimov.it.utils.logcat.wrappers.CRCSReportSendingFailedWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
public class CRCSReportSendingFailedTask extends Task<CRCSReportSendingFailedWrapper> {

    private static final String TAG = CRCSReportSendingFailedTask.class.getSimpleName();

    private static final String REPORT_SENDING_FAILED_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*sendFailed.*status=([A-Z_]*).*type=([A-Z_]*).*token=([0-9]*).*reasonCode: ([0-9]*)";
    private static Pattern reportSendingFailedPattern = Pattern.compile(REPORT_SENDING_FAILED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected CRCSReportSendingFailedWrapper parseLine(String line) {
        Matcher matcher = reportSendingFailedPattern.matcher(line);
        if (matcher.find()) {
            CRCSReportSendingFailedWrapper wrapper = new CRCSReportSendingFailedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setStatus(matcher.group(3));
            wrapper.setType(matcher.group(4));
            wrapper.setToken(matcher.group(5));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
