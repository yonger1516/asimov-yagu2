package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FclLHSRWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FclLHSRTask extends Task<FclLHSRWrapper> {
    private static final String TAG = FCLTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Constructed HTTPS LHSR:.*local_hs_res.*=(.*)";
    private static Pattern fclPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);

    protected FclLHSRWrapper parseLine(String line) {
        Matcher matcher = fclPattern.matcher(line);
        if (matcher.find()) {
            FclLHSRWrapper wrapper = new FclLHSRWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setLocalHsRes(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
