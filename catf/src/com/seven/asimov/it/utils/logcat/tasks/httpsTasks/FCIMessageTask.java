package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCIMessageWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 29.12.13
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public class FCIMessageTask extends Task<FCIMessageWrapper> {
    private static final String TAG = CCRTask.class.getSimpleName();

    private static final String FCI_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Received FCI message from OCEngine";
    private static Pattern fciPattern = Pattern.compile(FCI_REGEXP, Pattern.CASE_INSENSITIVE);


    protected FCIMessageWrapper parseLine(String line) {
        Matcher matcher = fciPattern.matcher(line);
        if (matcher.find()) {
            FCIMessageWrapper wrapper = new FCIMessageWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
