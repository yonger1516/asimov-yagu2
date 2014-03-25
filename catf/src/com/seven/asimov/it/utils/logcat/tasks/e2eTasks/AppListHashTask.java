package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.AppListHashWrapper;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppListHashTask extends Task<AppListHashWrapper> {
    private static final String TAG = AppListHashTask.class.getSimpleName();

    private static final String APPLICATION_LIST_HASH_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Application list hash: (.[a-zA-Z0-9]*) list.*";
    private static final Pattern appListHashPattern = Pattern.compile(APPLICATION_LIST_HASH_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected AppListHashWrapper parseLine(String line) {
        Matcher matcher = appListHashPattern.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            AppListHashWrapper wrapper = new AppListHashWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setListHash(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
