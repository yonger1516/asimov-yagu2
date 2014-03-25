package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.wrappers.TimerConditionExitedEnteredStateWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerConditionExitedEnteredStateTask extends Task<TimerConditionExitedEnteredStateWrapper> {

    private String exit_enteredState  = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Switching state: 'Exited' -> 'Entered'";
    Pattern exit_enteredStatePatten = Pattern.compile(exit_enteredState);

    @Override
    protected TimerConditionExitedEnteredStateWrapper parseLine(String line) {
        Matcher exit_enteredStateMatcher = exit_enteredStatePatten.matcher(line);
        if (exit_enteredStateMatcher.find()) {
            TimerConditionExitedEnteredStateWrapper timerConditionExitedEnteredStateWrapper = new TimerConditionExitedEnteredStateWrapper();
            setTimestampToWrapper(timerConditionExitedEnteredStateWrapper, exit_enteredStateMatcher);
            return timerConditionExitedEnteredStateWrapper;
        }
        return null;
    }
}
