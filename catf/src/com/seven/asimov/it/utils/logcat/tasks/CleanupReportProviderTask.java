package com.seven.asimov.it.utils.logcat.tasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.CleanupReportProviderWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanupReportProviderTask extends Task<CleanupReportProviderWrapper> {

    private static final String TAG = CleanupReportProviderTask.class.getSimpleName();

    private static final String CLEANUP_REPORT_PROVIDER_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*cleanup report provider done";

    private static final Pattern cleanupReportProviderPattern = Pattern.compile(CLEANUP_REPORT_PROVIDER_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public CleanupReportProviderWrapper parseLine(String line) {
        Matcher matcher = cleanupReportProviderPattern.matcher(line);
        if (matcher.find()) {
            CleanupReportProviderWrapper wrapper = new CleanupReportProviderWrapper();
            setTimestampToWrapper(wrapper, matcher);
            Log.d(TAG, "parseLine found line " + matcher.group(1));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
