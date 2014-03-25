package com.seven.asimov.it.utils.logcat.tasks.sessionBasicTasks;

import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ResponseMD5Wrapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseMD5Task extends Task<ResponseMD5Wrapper> {
    private static final String RESPONSE_MD5_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*(\\w+).*response MD5: \\[([\\d\\w]+)\\]";
    private final Pattern pattern = Pattern.compile(RESPONSE_MD5_REGEXP);

    @Override
    protected ResponseMD5Wrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return new ResponseMD5Wrapper(LogcatChecks.getUnixTimeFromString(matcher.group(1), matcher.group(2)), matcher.group(4));
        }
        return null;
    }
}