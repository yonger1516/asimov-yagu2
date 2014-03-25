package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CacheSizeWrapper;

import android.util.Log;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CacheSizeTask extends Task<CacheSizeWrapper> {
    private static final String TAG = CacheSizeTask.class.getSimpleName();

    private static final String CACHE_SIZE_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*ensureFreeSpace: Total cache size is\\s([0-9]*)";
    private static final Pattern cacheSize = Pattern.compile(CACHE_SIZE_REGEXP);

    @Override
    protected CacheSizeWrapper parseLine(String line) {
        Matcher matcher = cacheSize.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            CacheSizeWrapper wrapper = new CacheSizeWrapper(0, matcher.group(3));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

}
