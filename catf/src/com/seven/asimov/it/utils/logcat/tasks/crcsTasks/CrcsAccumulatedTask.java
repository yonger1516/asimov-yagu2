package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.CrcsAccumulatedWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrcsAccumulatedTask extends Task<CrcsAccumulatedWrapper> {

    private static final String TAG = CrcsAccumulatedTask.class.getSimpleName();

    private static final String ACCUMULATED_RECORDS_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).* ([0-9]*) CRCS records already accumulated, going to transfer...";

    private static final Pattern accumulatedRecordsPattern = Pattern.compile(ACCUMULATED_RECORDS_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public CrcsAccumulatedWrapper parseLine(String line) {
        Matcher matcher = accumulatedRecordsPattern.matcher(line);
        if (matcher.find()) {
            CrcsAccumulatedWrapper wrapper = new CrcsAccumulatedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setCount(Integer.parseInt(matcher.group(3)));
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