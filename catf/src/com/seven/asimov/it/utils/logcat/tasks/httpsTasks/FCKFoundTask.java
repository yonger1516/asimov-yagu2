package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCKFoundWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/9/13
 * Time: 8:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class FCKFoundTask extends Task<FCKFoundWrapper> {
//TODO extends wrapper
private static final String FCL_REGEXP =
        "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*FC.*FCK..([A-Z0-9]*).*found in the storage";
private static Pattern fckPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);


protected FCKFoundWrapper parseLine(String line) {
        Matcher matcher = fckPattern.matcher(line);
if (matcher.find()) {
    FCKFoundWrapper wrapper = new FCKFoundWrapper();
setTimestampToWrapper(wrapper, matcher);
wrapper.setFck(matcher.group(3));
return wrapper;
}
return null;
}
}
