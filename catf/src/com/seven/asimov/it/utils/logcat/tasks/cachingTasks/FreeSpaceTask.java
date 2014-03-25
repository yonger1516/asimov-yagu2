package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.wrappers.FreeSpaceWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FreeSpaceTask extends Task<FreeSpaceWrapper> {
    private static final String TAG = FreeSpaceTask.class.getSimpleName();
    private static final String FREE_SPACE_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Done checking available space on the device. Free space:\\s([0-9]*).*";
    private static final Pattern freeSpace = Pattern.compile(FREE_SPACE_REGEXP);

    @Override
    protected FreeSpaceWrapper parseLine(String line) {
        Matcher matcher = freeSpace.matcher(line);
        if (matcher.find()) {
            //TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            //Log.d(TAG, "Set timezone to GMT");
            FreeSpaceWrapper wrapper = new FreeSpaceWrapper(0, matcher.group(3));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

}
