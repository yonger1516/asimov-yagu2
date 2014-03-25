package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.TimerConditionEnteredDisabledStateWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TimerConditionEnteredDisabledStateTask extends Task<TimerConditionEnteredDisabledStateWrapper> {
    private String disable_exitedState  = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Switching state: 'Entered' -> 'Disabled'";
    Pattern disable_exitedStatePattern = Pattern.compile(disable_exitedState);

    @Override
    public TimerConditionEnteredDisabledStateWrapper parseLine(String line) {
        Matcher exit_enteredStateMatcher = disable_exitedStatePattern.matcher(line);
        if (exit_enteredStateMatcher.find()) {
            TimerConditionEnteredDisabledStateWrapper timerConditionEnteredDisabledStateWrapper = new TimerConditionEnteredDisabledStateWrapper();
            setTimestampToWrapper(timerConditionEnteredDisabledStateWrapper, exit_enteredStateMatcher);
            return timerConditionEnteredDisabledStateWrapper;
        }
        return null;
    }
}
