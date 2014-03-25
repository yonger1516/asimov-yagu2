package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.IWCWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IWCTask extends Task<IWCWrapper> {
    private static final String IWC_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*saved into cache";
    private static final Pattern IWCPattern = Pattern.compile(IWC_REGEXP,Pattern.CASE_INSENSITIVE);

    @Override
    protected IWCWrapper parseLine(String line) {
        Matcher matcher = IWCPattern.matcher(line);
        if (matcher.find()) {
            IWCWrapper wrapper = new IWCWrapper();
            setTimestampToWrapper(wrapper, matcher);
            Log.d(IWCWrapper.class.getName(), "Find line " + wrapper.getTimestamp());
            return wrapper;
        }
        return null;
    }
}

