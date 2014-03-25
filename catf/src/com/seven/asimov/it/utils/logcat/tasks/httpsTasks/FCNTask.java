package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCNWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/7/13
 * Time: 7:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class FCNTask extends Task<FCNWrapper> {   private static final String TAG = FCLTask.class.getSimpleName();
     //TODO extends wrapper
    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).*: verdict (FCN) \\(IP ([0-9.]*).*capabilities ([0-9]*).*";
    private static Pattern fcnPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);


    protected FCNWrapper parseLine(String line) {
        Matcher matcher = fcnPattern.matcher(line);
        if (matcher.find()) {
            FCNWrapper wrapper = new FCNWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setVerdict(matcher.group(3));
            wrapper.setIP(matcher.group(4));
            wrapper.setCapabilities(Integer.parseInt(matcher.group(5)));
            return wrapper;
        }
        return null;
    }
}

