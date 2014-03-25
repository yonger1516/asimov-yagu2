package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.FCLWrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FCLTask extends Task<FCLWrapper> {
    private static final String TAG = FCLTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Constructed HTTPS FCL from .* DST ([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})\\:([0-9]{1,5})";
    private static Pattern fclPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);

    protected FCLWrapper parseLine(String line) {
        Matcher matcher = fclPattern.matcher(line);
        if (matcher.find()) {
            FCLWrapper wrapper = new FCLWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setIp(matcher.group(3));
            wrapper.setPort(Integer.parseInt(matcher.group(4)));
            return wrapper;
        }
        return null;
    }
}
