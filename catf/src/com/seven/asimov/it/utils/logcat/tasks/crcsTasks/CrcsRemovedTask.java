package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CrcsRemovedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class CrcsRemovedTask extends Task<CrcsRemovedWrapper> {

    private static final String TAG = CrcsRemovedTask.class.getSimpleName();

    private static final String CRCS_REMOVED_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*CRCS records successfully removed from database";

    private static final Pattern crcsRecordRemovedPattern = Pattern.compile(CRCS_REMOVED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public CrcsRemovedWrapper parseLine(String line) {
        Matcher matcher = crcsRecordRemovedPattern.matcher(line);
        if (matcher.find()) {
            CrcsRemovedWrapper wrapper = new CrcsRemovedWrapper();
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
