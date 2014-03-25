package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCPWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/9/13
 * Time: 5:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class FCPTask extends Task<FCPWrapper> {
    //TODO extends wrapper
    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).*- FC \\[([A-Z0-9]*)\\]: verdict FCP";
    private static Pattern fcpPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);


    protected FCPWrapper parseLine(String line) {
        Matcher matcher = fcpPattern.matcher(line);
        if (matcher.find()) {
            FCPWrapper wrapper = new FCPWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setOriginator(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}