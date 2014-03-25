package com.seven.asimov.it.utils.logcat.tasks.debugDataCollectorTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DebugDataSizeTriggerWrapper;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DebugDataSizeTriggerTask extends Task <DebugDataSizeTriggerWrapper> {
    private static final String TAG = DebugDataSizeTriggerTask.class.getSimpleName();

    private static final String SIZE_TRIGGER =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*debug_data_manager.cpp.*Debug data upload size trigger is active, size=([0-9]*).*";
    private static final Pattern sizeTrigger = Pattern.compile(SIZE_TRIGGER);

    @Override
    protected DebugDataSizeTriggerWrapper parseLine(String line) {
        Matcher matcher = sizeTrigger.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            DebugDataSizeTriggerWrapper wrapper = new DebugDataSizeTriggerWrapper(0);
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
