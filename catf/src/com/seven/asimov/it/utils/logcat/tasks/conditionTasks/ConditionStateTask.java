package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.ConditionState;
import com.seven.asimov.it.utils.logcat.wrappers.ConditionStateWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionStateTask extends Task<ConditionStateWrapper> {
    private static final String TAG = ConditionStateTask.class.getSimpleName();
    private String conditionStateRegexp = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*AppProfile '%s'.*Script '%s'.*Switching state: '%s' -> '%s'";
    private Pattern conditionStatePattern;

    private String packageName;
    private String scriptName;
    private ConditionState fromState;
    private ConditionState toState;

    public ConditionStateTask(String packageName, String scriptName, ConditionState fromState, ConditionState toState) {
        this.packageName = packageName;
        this.scriptName = scriptName;
        this.fromState = fromState;
        this.toState = toState;

        conditionStateRegexp = String.format(conditionStateRegexp,
                packageName != null ? packageName : "([a-z.]*)",
                scriptName != null ? scriptName : "(.*)",
                fromState != null ? fromState.toString() : "(.*)",
                toState != null ? toState.toString() : "(.*)");
        Log.v(TAG, "conditionStateRegexp=" + conditionStateRegexp);
        conditionStatePattern = Pattern.compile(conditionStateRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public ConditionStateWrapper parseLine(String line) {
        Matcher matcher = conditionStatePattern.matcher(line);
        int groupNumber = 3;
        if (matcher.find()) {
            ConditionStateWrapper wrapper = new ConditionStateWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setPackageName(packageName != null ? packageName : matcher.group(groupNumber++));
            wrapper.setScriptName(scriptName != null ? scriptName : matcher.group(groupNumber++));
            wrapper.setFromState(fromState != null ? fromState.toString() : matcher.group(groupNumber++));
            wrapper.setToState(toState != null ? toState.toString() : matcher.group(groupNumber));
            //Log.d(TAG, "parseLine found line " + matcher.group(1));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return new StringBuilder(TAG)
                .append(" packageName:").append(packageName)
                .append(" scriptName:").append(scriptName)
                .append(" fromState:").append(fromState)
                .append(" toState:").append(toState).append(": ").toString();
    }
}
