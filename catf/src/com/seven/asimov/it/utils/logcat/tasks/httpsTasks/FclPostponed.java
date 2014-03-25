package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FclPostponedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FclPostponed extends Task<FclPostponedWrapper> {
    private static final String TAG = FCLTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*FCL processing.*\\(CSM (\\[.*\\]) FCK (\\[.*\\]\\)) postponed till CCV for CSM(\\[.*\\])";
    private static Pattern fclPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);

    protected FclPostponedWrapper parseLine(String line) {
        Matcher matcher = fclPattern.matcher(line);
        if (matcher.find()) {
            FclPostponedWrapper wrapper = new FclPostponedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setCsm(matcher.group(3));
            wrapper.setFck(matcher.group(4));
            wrapper.setForCsm(matcher.group(5));
            return wrapper;
        }
        return null;
    }
}
