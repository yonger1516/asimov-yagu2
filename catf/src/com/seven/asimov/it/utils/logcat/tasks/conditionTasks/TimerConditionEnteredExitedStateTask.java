package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.wrappers.TimerConditionEnteredExitedStateWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerConditionEnteredExitedStateTask extends Task<TimerConditionEnteredExitedStateWrapper> {

    private String disable_exitedState  = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Switching state: 'Entered' -> 'Exited'";
    Pattern disable_exitedStatePattern = Pattern.compile(disable_exitedState);

    @Override
    public TimerConditionEnteredExitedStateWrapper parseLine(String line) {
        Matcher exit_enteredStateMatcher = disable_exitedStatePattern.matcher(line);
        if (exit_enteredStateMatcher.find()) {
            TimerConditionEnteredExitedStateWrapper timerConditionEnteredExitedStateWrapper = new TimerConditionEnteredExitedStateWrapper();
            setTimestampToWrapper(timerConditionEnteredExitedStateWrapper, exit_enteredStateMatcher);
            return timerConditionEnteredExitedStateWrapper;
        }
        return null;
    }
}
