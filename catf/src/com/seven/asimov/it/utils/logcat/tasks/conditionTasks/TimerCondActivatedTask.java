package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.tasks.e2eTasks.AddedAppTask;
import com.seven.asimov.it.utils.logcat.wrappers.TimerCondActivatedWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerCondActivatedTask extends Task<TimerCondActivatedWrapper> {
    private static final String TAG = AddedAppTask.class.getSimpleName();

    private static final String TIMER_ACTIVATED_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).+Timer condition activated \\(group=(\\S+), script=(\\S+)\\)";
    private static final Pattern atimerActivatedPattern = Pattern.compile(TIMER_ACTIVATED_REGEXP);

    @Override
    protected TimerCondActivatedWrapper parseLine(String line) {
        Matcher matcher = atimerActivatedPattern.matcher(line);
        if (matcher.find()) {
            TimerCondActivatedWrapper wrapper = new TimerCondActivatedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setGroup(matcher.group(3));
            wrapper.setScript(matcher.group(4));
            return wrapper;
        }
        return null;
    }
}
