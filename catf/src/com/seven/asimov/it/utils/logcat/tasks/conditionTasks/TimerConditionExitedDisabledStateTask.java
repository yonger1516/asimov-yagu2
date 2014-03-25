package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.wrappers.TimerConditionExitedDisabledStateWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerConditionExitedDisabledStateTask extends Task<TimerConditionExitedDisabledStateWrapper> {

    String exited_disabledState = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Switching state: 'Exited' -> 'Disabled'";
    Pattern exited_disabledStatePattern = Pattern.compile(exited_disabledState);

    @Override
    public TimerConditionExitedDisabledStateWrapper parseLine(String line) {
        Matcher exited_disabledStateMatcher = exited_disabledStatePattern.matcher(line);
        if (exited_disabledStateMatcher.find()) {
            TimerConditionExitedDisabledStateWrapper timerConditionExitedDisabledStateWrapper = new TimerConditionExitedDisabledStateWrapper();
            setTimestampToWrapper(timerConditionExitedDisabledStateWrapper, exited_disabledStateMatcher);
            return timerConditionExitedDisabledStateWrapper;
        }
        return null;
    }
}
