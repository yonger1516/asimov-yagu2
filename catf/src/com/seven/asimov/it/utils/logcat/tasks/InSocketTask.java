package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.tasks.cachingTasks.CsaTask;
import com.seven.asimov.it.utils.logcat.wrappers.InSocketWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InSocketTask extends Task<InSocketWrapper> {

    private static final String TAG = CsaTask.class.getSimpleName();

    private static final String IN_SOCKET_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*HTRX \\[([0-9A-Z]*).*IN socket closed by peer";
    private static final Pattern IN_SOCKET_PATTERN = Pattern.compile(IN_SOCKET_REGEXP,Pattern.CASE_INSENSITIVE);


    protected InSocketWrapper parseLine(String line){
        Matcher matcher = IN_SOCKET_PATTERN.matcher(line);
        if (matcher.find()){
            InSocketWrapper wrapper = new InSocketWrapper();
            setTimestampToWrapper(wrapper,matcher);
            wrapper.setHtrx(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
