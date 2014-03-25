package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.wrappers.CSNWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsnTask extends Task<CSNWrapper> {

    private static final String TAG = CsnTask.class.getSimpleName();

    private static final String CSN_REGEXP_V1 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSN.*Dispatcher\\s\\[([0-9a-z]*)";
    private static final Pattern csnPattern_V1 = Pattern.compile(CSN_REGEXP_V1, Pattern.CASE_INSENSITIVE);

    private static final String CSN_REGEXP_V2 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSN.*originator http\\s\\[([0-9a-z]*)";
    private static final Pattern csnPattern_V2 = Pattern.compile(CSN_REGEXP_V2, Pattern.CASE_INSENSITIVE);

    private static final String CSN_REGEXP_V3 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSN.*originator ocshttpd\\s\\[([0-9a-z]*)";
    private static final Pattern csnPattern_V3 = Pattern.compile(CSN_REGEXP_V3, Pattern.CASE_INSENSITIVE);

    private static final String CSN_REGEXP_V4 = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).* Executed task.*CSN.*originator ochttpd\\s\\[([0-9a-z]*)";
    private static final Pattern csnPattern_V4 = Pattern.compile(CSN_REGEXP_V4, Pattern.CASE_INSENSITIVE);


    protected CSNWrapper parseLine(String line) {
        Matcher matcher1 = csnPattern_V1.matcher(line);
        if (matcher1.find()) {
            CSNWrapper wrapper = new CSNWrapper();
            setTimestampToWrapper(wrapper, matcher1);
            wrapper.setHtrx(matcher1.group(3));
            return wrapper;
        }
        Matcher matcher2 = csnPattern_V2.matcher(line);
        if (matcher2.find()) {
            CSNWrapper wrapper = new CSNWrapper();
            setTimestampToWrapper(wrapper, matcher2);
            wrapper.setHtrx(matcher2.group(3));
            return wrapper;
        }
        Matcher matcher3 = csnPattern_V3.matcher(line);
        if (matcher3.find()) {
            CSNWrapper wrapper = new CSNWrapper();
            setTimestampToWrapper(wrapper, matcher3);
            wrapper.setHtrx(matcher3.group(3));
            return wrapper;
        }
        Matcher matcher4 = csnPattern_V4.matcher(line);
        if (matcher4.find()) {
            CSNWrapper wrapper = new CSNWrapper();
            setTimestampToWrapper(wrapper, matcher4);
            wrapper.setHtrx(matcher4.group(3));
            return wrapper;
        }
        return null;
    }
}
