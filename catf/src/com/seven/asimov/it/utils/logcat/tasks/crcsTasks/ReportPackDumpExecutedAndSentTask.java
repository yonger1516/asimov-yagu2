package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ReportPackDumpExecutedAndSentWrapper;

public class ReportPackDumpExecutedAndSentTask extends Task<ReportPackDumpExecutedAndSentWrapper> {

    private static final String TAG = ReportPackDumpExecutedAndSentTask.class.getSimpleName();

    private static final String REPORT_DUMP_TRANSACTION_EXECUTED_AND_SENT = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Report pack dump executed and sent successfully!";

    private static final Pattern reportDumpTransactionExecutedAndSentPattern = Pattern.compile(REPORT_DUMP_TRANSACTION_EXECUTED_AND_SENT, Pattern.CASE_INSENSITIVE);

    @Override
    protected ReportPackDumpExecutedAndSentWrapper parseLine(String line) {
        Matcher matcher = reportDumpTransactionExecutedAndSentPattern.matcher(line);
        if (matcher.find()) {
            ReportPackDumpExecutedAndSentWrapper wrapper = new ReportPackDumpExecutedAndSentWrapper();
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
