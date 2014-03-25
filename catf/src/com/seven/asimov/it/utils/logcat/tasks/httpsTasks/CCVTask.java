package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CCVWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/9/13
 * Time: 10:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class CCVTask extends Task<CCVWrapper> {
    private static final String TAG = CCVTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Constructed HTTPS CCV:.*FCK([A-Z0-9]*).*payload size ([0-9]*)";
    private static Pattern ccvPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);


    protected CCVWrapper parseLine(String line) {
        Matcher matcher = ccvPattern.matcher(line);
        if (matcher.find()) {
            CCVWrapper wrapper = new CCVWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setFck(matcher.group(3));
            wrapper.setPayloadSize(Integer.parseInt(matcher.group(4)));
            return wrapper;
        }
        return null;
    }
}