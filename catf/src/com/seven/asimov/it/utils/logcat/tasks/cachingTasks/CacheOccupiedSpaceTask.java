package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;


import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CacheOccupiedSpaceWrapper;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheOccupiedSpaceTask extends Task <CacheOccupiedSpaceWrapper> {
    private static final String TAG = CacheOccupiedSpaceTask.class.getSimpleName();
    private static final String OCCUPIED_SPACE_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Cache Stats: Occupied Space:\\s([0-9]*).*";
    private static final Pattern occupiedSpace = Pattern.compile(OCCUPIED_SPACE_REGEXP);

    @Override
    protected CacheOccupiedSpaceWrapper parseLine(String line) {
        Matcher matcher = occupiedSpace.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            CacheOccupiedSpaceWrapper wrapper = new CacheOccupiedSpaceWrapper(0, matcher.group(3));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

}
