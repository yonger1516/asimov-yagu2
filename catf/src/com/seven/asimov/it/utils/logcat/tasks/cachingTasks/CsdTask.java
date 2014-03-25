package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CSDWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsdTask extends Task<CSDWrapper> {

    private static final String TAG = CsdTask.class.getSimpleName();

    private static final String CSD_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*HTRX \\[([0-9A-Z]*).*verdict CSD";
    private static final Pattern csdPattern = Pattern.compile(CSD_REGEXP, Pattern.CASE_INSENSITIVE);


    protected CSDWrapper parseLine(String line) {
        Matcher matcher = csdPattern.matcher(line);
        if (matcher.find()) {
            CSDWrapper wrapper = new CSDWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setHtrx(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
