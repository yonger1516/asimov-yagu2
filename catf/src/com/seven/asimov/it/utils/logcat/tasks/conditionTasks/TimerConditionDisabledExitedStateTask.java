package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.wrappers.TimerConditionDisabledExitedStateWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerConditionDisabledExitedStateTask extends Task<TimerConditionDisabledExitedStateWrapper> {

    private String disable_exitedState  = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Switching state: 'Disabled' -> .([a-zA-z]*.[a-zA-z]*.[a-zA-z]*).";
    private String disable_exitedState_with_configuration  = "";
    Pattern disable_exitedStatePattern = Pattern.compile(disable_exitedState);
    Pattern disable_exitedState_with_configurationPattern = Pattern.compile(disable_exitedState_with_configuration);

    @Override
    public TimerConditionDisabledExitedStateWrapper parseLine(String line) {
        Matcher disabled_exitedStateMatcher = disable_exitedStatePattern.matcher(line);
        Matcher disable_exitedState_with_configurationMatcher = disable_exitedState_with_configurationPattern.matcher(line);
        if (disabled_exitedStateMatcher.find()) {
            TimerConditionDisabledExitedStateWrapper timerConditionDisabledExitedStateWrapper = new TimerConditionDisabledExitedStateWrapper();
            setTimestampToWrapper(timerConditionDisabledExitedStateWrapper, disabled_exitedStateMatcher);
            return timerConditionDisabledExitedStateWrapper;
        }
        return null;
    }
}
