package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CrcsSendingTaskSuccessRemovedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class CrcsSendingTaskSuccessRemovedTask extends Task<CrcsSendingTaskSuccessRemovedWrapper> {

    private static final String TAG = CrcsSendingTaskSuccessRemovedTask.class.getSimpleName();

    private static String CRCS_SENDING_TASK_SUCCESSFULLY_REMOVED_REGEXP = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Task Daily CRCS Sending Task .(0[xX][0-9a-fA-F]*).* successfully removed";

    private static final Pattern crcsSendingTaskSuccessfullyRemovedPattern = Pattern.compile(CRCS_SENDING_TASK_SUCCESSFULLY_REMOVED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public CrcsSendingTaskSuccessRemovedWrapper parseLine(String line) {
        Matcher matcher = crcsSendingTaskSuccessfullyRemovedPattern.matcher(line);
        if (matcher.find()) {
            CrcsSendingTaskSuccessRemovedWrapper wrapper = new CrcsSendingTaskSuccessRemovedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setId(Integer.parseInt(matcher.group(3).substring(2), 16));
            //Log.d(TAG, "wrapper id = " + matcher.group(4) + " = " + wrapper.getId());
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
