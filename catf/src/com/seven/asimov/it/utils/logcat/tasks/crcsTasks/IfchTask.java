package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.IfchWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfchTask extends Task<IfchWrapper> {

    private static final String TAG = IfchTask.class.getSimpleName();

    private static final String IFCH_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*IFCH.*interface to ([a-z_]*).*TS=([0-9]*\\.[0-9]*)";
    private static final Pattern ifchPatern = Pattern.compile(IFCH_REGEXP, Pattern.CASE_INSENSITIVE);

    protected IfchWrapper parseLine(String line) {
        Matcher matcher = ifchPatern.matcher(line);
        if (matcher.find()) {
            IfchWrapper result = new IfchWrapper();
            setTimestampToWrapper(result, matcher);
            result.setInterfaceType(matcher.group(3));
            result.setTimeAfterStartAndroid(Double.parseDouble(matcher.group(4)));
            return result;
        }
        return null;
    }
}
