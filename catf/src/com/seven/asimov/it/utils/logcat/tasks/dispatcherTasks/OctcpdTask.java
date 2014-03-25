package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.OctcpdWrapper;

import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OctcpdTask extends Task <OctcpdWrapper> {
    private static final String TAG = OctcpdTask.class.getSimpleName();
    private static final String PORTS_REGEXP = "DROP\\s*tcp\\s\\s--\\s\\s0\\.0\\.0\\.0\\/0\\s*0\\.0\\.0\\.0\\/0\\s*ctorigdst\\s127\\.0\\.0\\.1\\sctorigdstport\\s([0-9]*)";
    private static final Pattern port = Pattern.compile(PORTS_REGEXP);

    @Override
    protected OctcpdWrapper parseLine(String line) {
        Matcher matcher = port.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            OctcpdWrapper wrapper = new OctcpdWrapper(0, matcher.group(1));
            //setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
