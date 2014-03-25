package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.wrappers.CSPWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CspTask extends Task<CSPWrapper> {

    private static final String TAG = CspTask.class.getSimpleName();

    private static final String CSP_REGEXP1 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSP.*Dispatcher\\s\\[([0-9a-z]*)";
    private static final Pattern cspPattern1 = Pattern.compile(CSP_REGEXP1, Pattern.CASE_INSENSITIVE);

    private static final String CSP_REGEXP2 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSP.*originator http\\s\\[([0-9a-z]*)";
    private static final Pattern cspPattern2 = Pattern.compile(CSP_REGEXP2, Pattern.CASE_INSENSITIVE);

    private static final String CSP_REGEXP3 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSP.*originator ocshttpd\\s\\[([0-9a-z]*)";
    private static final Pattern cspPattern3 = Pattern.compile(CSP_REGEXP3, Pattern.CASE_INSENSITIVE);

    private static final String CSP_REGEXP4 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSP.*originator ochttpd\\s\\[([0-9a-z]*)";
    private static final Pattern cspPattern4 = Pattern.compile(CSP_REGEXP4, Pattern.CASE_INSENSITIVE);

    protected CSPWrapper parseLine(String line) {
        Matcher matcher1 = cspPattern1.matcher(line);
        if (matcher1.find()) {
            CSPWrapper wrapper = new CSPWrapper();
            setTimestampToWrapper(wrapper, matcher1);
            wrapper.setHtrx(matcher1.group(3));
            return wrapper;
        }
        Matcher matcher2 = cspPattern2.matcher(line);
        if (matcher2.find()) {
            CSPWrapper wrapper = new CSPWrapper();
            setTimestampToWrapper(wrapper, matcher2);
            wrapper.setHtrx(matcher2.group(3));
            return wrapper;
        }
        Matcher matcher3 = cspPattern3.matcher(line);
        if (matcher3.find()) {
            CSPWrapper wrapper = new CSPWrapper();
            setTimestampToWrapper(wrapper, matcher3);
            wrapper.setHtrx(matcher3.group(3));
            return wrapper;
        }
        Matcher matcher4 = cspPattern4.matcher(line);
        if (matcher4.find()) {
            CSPWrapper wrapper = new CSPWrapper();
            setTimestampToWrapper(wrapper, matcher4);
            wrapper.setHtrx(matcher4.group(3));
            return wrapper;
        }
        return null;
    }
}
