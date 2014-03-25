package com.seven.asimov.it.utils.logcat.tasks.debugDataCollectorTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DebugDataTimeTriggerWrapper;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebugDataTimeTriggerTask extends Task <DebugDataTimeTriggerWrapper> {
    private static final String TAG = DebugDataTimeTriggerTask.class.getSimpleName();

    private static final String DEBUG_DATA_UPLOADING =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*debug_data_manager.cpp.*Debug data upload time trigger is active.*";
    private static final Pattern uploader = Pattern.compile(DEBUG_DATA_UPLOADING);

    @Override
    protected DebugDataTimeTriggerWrapper parseLine(String line) {
        Matcher matcher = uploader.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            DebugDataTimeTriggerWrapper wrapper = new DebugDataTimeTriggerWrapper(0);
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
