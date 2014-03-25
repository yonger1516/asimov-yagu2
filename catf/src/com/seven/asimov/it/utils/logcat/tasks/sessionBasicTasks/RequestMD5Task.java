package com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.RequestMD5Wrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestMD5Task extends Task<RequestMD5Wrapper> {

    private static final String REQUEST_MD5_REGEXP =
            "(201[2-9].[0-1][0-9].[0-3][0-9].[0-2]*[0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]{3})[0-9]* (\\w+).*" +
                    "Normalized request headers and body \\(if available\\) MD5: \\[([\\d\\w]+)\\]";
    private final Pattern pattern = Pattern.compile(REQUEST_MD5_REGEXP);

    @Override
    protected RequestMD5Wrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            RequestMD5Wrapper wrapper = new RequestMD5Wrapper(0, matcher.group(3));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
