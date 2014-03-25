package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ScriptLogWrapper;


public class ScriptLogTask extends Task<ScriptLogWrapper> {
    private static final String TAG = ScriptLogTask.class.getSimpleName();

    private static final String SL_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).+ScriptLog: (.+),(.+),(\\d+),(.+),(.+),(\\d+),(\\d+),(-?\\d+),(.*),(\\d+)";
    private static Pattern ScriptLogPattern = Pattern.compile(SL_REGEXP, Pattern.CASE_INSENSITIVE);



    protected ScriptLogWrapper parseLine(String line) {
        Matcher matcher = ScriptLogPattern.matcher(line);

        if (matcher.find()) {
            ScriptLogWrapper wrapper = new ScriptLogWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setLogTime(matcher.group(3));
            wrapper.setTransactType(matcher.group(4));
            wrapper.setVersion(Integer.parseInt(matcher.group(5)));
            wrapper.setAppName(matcher.group(6));
            wrapper.setScriptName(matcher.group(7));
            wrapper.setState(Integer.parseInt(matcher.group(8)));
            wrapper.setEvent(Integer.parseInt(matcher.group(9)));
            wrapper.setErrorCode(Integer.parseInt(matcher.group(10)));
            wrapper.setEventData(matcher.group(11));
            wrapper.setSequenceNumber(Long.parseLong(matcher.group(12)));
            return wrapper;
        }
        return null;
    }
}

