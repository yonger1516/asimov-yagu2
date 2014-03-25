package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ForceCRSCReportSendingWrapper;


public class ForceCRSCReportSendingTask extends Task<ForceCRSCReportSendingWrapper> {
    private final static String FORCE_CRCS_REPORT_SENDING_REGEXP = "(RadioLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9]." +
            "[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),(radio,.),([_a-zA-Z]+),([_a-zA-Z]+),([-0-9]+),([-0-9]+)";
    private final static Pattern forceCRCSReportSendingPattern = Pattern.compile(FORCE_CRCS_REPORT_SENDING_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected ForceCRSCReportSendingWrapper parseLine(String line) {
        ForceCRSCReportSendingWrapper wrapper = new ForceCRSCReportSendingWrapper();
        Matcher matcher = forceCRCSReportSendingPattern.matcher(line);
        if (matcher.find()) {
            return wrapper;
        }
        return null;
    }
}
