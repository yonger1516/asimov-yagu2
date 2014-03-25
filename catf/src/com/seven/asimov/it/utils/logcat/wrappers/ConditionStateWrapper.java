package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 7/29/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConditionStateWrapper extends LogEntryWrapper {
    private static final String TAG = ConditionStateWrapper.class.getSimpleName();

    private String packageName;
    private String scriptName;
    private String fromState;
    private String toState;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getFromState() {
        return fromState;
    }

    public void setFromState(String fromState) {
        this.fromState = fromState;
    }

    public String getToState() {
        return toState;
    }

    public void setToState(String toState) {
        this.toState = toState;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).
                append(" packageName: ").append(packageName).
                append(" scriptName: ").append(scriptName).
                append(" fromState: ").append(fromState).
                append(" toState: ").append(toState).toString();
    }
}
