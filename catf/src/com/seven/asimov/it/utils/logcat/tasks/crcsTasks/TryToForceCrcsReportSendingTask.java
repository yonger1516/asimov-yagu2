package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.TryToForceCrcsReportSendingWrapper;

public class TryToForceCrcsReportSendingTask extends Task<TryToForceCrcsReportSendingWrapper> {
    private static final String TAG = TryToForceCrcsReportSendingTask.class.getSimpleName();

    private static final String TRY_TO_FORCE_REGEX = "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).*Try to force CRCS report sending...";

    private static final Pattern tryToForcePattern = Pattern.compile(TRY_TO_FORCE_REGEX);

    @Override
    protected TryToForceCrcsReportSendingWrapper parseLine(String line) {
        Matcher matcher = tryToForcePattern.matcher(line);
        if (matcher.find()) {
            TryToForceCrcsReportSendingWrapper wrapper = new TryToForceCrcsReportSendingWrapper();
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
