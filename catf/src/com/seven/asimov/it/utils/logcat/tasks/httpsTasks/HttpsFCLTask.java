package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.HttpsFCLWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpsFCLTask extends Task<HttpsFCLWrapper> {
    private static final String TAG = FCLTask.class.getSimpleName();

    private static final String FCL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Constructed HTTPS FCL from .*: FC (\\[.*\\]), DST (.*), UID (.*), hostname: (.*)";
    private static Pattern fclPattern = Pattern.compile(FCL_REGEXP, Pattern.CASE_INSENSITIVE);

    protected HttpsFCLWrapper parseLine(String line) {
        Matcher matcher = fclPattern.matcher(line);
        if (matcher.find()) {
            HttpsFCLWrapper wrapper = new HttpsFCLWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setFc(matcher.group(3));
            wrapper.setDst(matcher.group(4));
            wrapper.setUid(matcher.group(5));
            return wrapper;
        }
        return null;
    }
}
