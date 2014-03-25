package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PrepareToCleanupReportProviderWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PrepareToCleanupReportProviderTask extends Task <PrepareToCleanupReportProviderWrapper> {

    private static final String TAG = PrepareToCleanupReportProviderTask.class.getSimpleName();

    private static final String PREPARE_TO_CLEANUP_REGEX = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*prepare to cleanup report provider...";

    private static final Pattern prepareToCleanupReportProviderPattern = Pattern.compile(PREPARE_TO_CLEANUP_REGEX);

    @Override
    protected PrepareToCleanupReportProviderWrapper parseLine(String line) {
        Matcher matcher = prepareToCleanupReportProviderPattern.matcher(line);
        if (matcher.find()) {
            PrepareToCleanupReportProviderWrapper wrapper = new PrepareToCleanupReportProviderWrapper();
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
