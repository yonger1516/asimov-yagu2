package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CCRWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/10/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class CCRTask extends Task<CCRWrapper> {
    private static final String TAG = CCRTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).*FCK \\[([A-Z0-9]*).*verdict CCR ([0-9]) \\(([a-z ]*)\\)";
    private static Pattern ccrPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);


    protected CCRWrapper parseLine(String line) {
        Matcher matcher = ccrPattern.matcher(line);
        if (matcher.find()) {
            CCRWrapper wrapper = new CCRWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setFck(matcher.group(3));
            wrapper.setCodeVerdict(Integer.parseInt(matcher.group(4)));
            wrapper.setStringVerdict(matcher.group(5));
            return wrapper;
        }
        return null;
    }




}
