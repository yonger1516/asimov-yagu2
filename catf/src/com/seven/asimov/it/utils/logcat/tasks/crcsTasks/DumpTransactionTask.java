package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.DumpTransactionWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DumpTransactionTask extends Task<DumpTransactionWrapper> {

    private static final String TAG = DumpTransactionTask.class.getSimpleName();

    private static final String reportingDumpTransactionRequestRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* a client reporting dump transactions (request|response).*of ([0-9]*)";

    private static final Pattern dumpTransactionPattern = Pattern.compile(reportingDumpTransactionRequestRegexp, Pattern.CASE_INSENSITIVE);

    @Override
    public DumpTransactionWrapper parseLine(String line) {
        Matcher matcher = dumpTransactionPattern.matcher(line);
        if (matcher.find()) {
            DumpTransactionWrapper wrapper = new DumpTransactionWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setDirection(matcher.group(3));
            wrapper.setBytes(Integer.parseInt(matcher.group(4)));
            if ("request".equalsIgnoreCase(matcher.group(3)) || "response".equalsIgnoreCase(matcher.group(3))) {
                //Log.d(TAG, "parseLine found line " + matcher.group(1));
                return wrapper;
            }
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
