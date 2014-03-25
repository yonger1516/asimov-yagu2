package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CSAWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsaTask extends Task<CSAWrapper> {

    private static final String TAG = CsaTask.class.getSimpleName();

    private static final String CSA_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*HTRX \\[([0-9A-Z]*).*verdict CSA";
    private static final Pattern csaPattern = Pattern.compile(CSA_REGEXP, Pattern.CASE_INSENSITIVE);


    protected CSAWrapper parseLine(String line) {
        Matcher matcher = csaPattern.matcher(line);
        if (matcher.find()) {
            CSAWrapper wrapper = new CSAWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setHtrx(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
