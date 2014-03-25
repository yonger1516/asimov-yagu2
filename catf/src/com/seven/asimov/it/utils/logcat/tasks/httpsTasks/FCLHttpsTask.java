package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCLWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FCLHttpsTask extends Task<FCLWrapper> {
    private boolean checkFCgeneration = false;
    private static final String TAG = FCLTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(20[1-9]{2}/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Constructed HTTPS FCL from ocshttpd: FC (\\[.*\\]).*";
    private static Pattern fclPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String FCGENERATION =
            "(20[1-9]{2}/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Success FC generation for CSM(\\[.*\\]).*";
    private static Pattern fcGeneration = Pattern.compile(FCGENERATION, Pattern.CASE_INSENSITIVE);

    public FCLHttpsTask(boolean checkFCgeneration){
        this.checkFCgeneration =  checkFCgeneration;
    }

    protected FCLWrapper parseLine(String line) {
        Matcher matcher;
        if (checkFCgeneration){
            matcher = fcGeneration.matcher(line);
        }
        else {
            matcher = fclPattern.matcher(line);
        }
        if (matcher.find()) {
            FCLWrapper wrapper = new FCLWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setCSM(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
