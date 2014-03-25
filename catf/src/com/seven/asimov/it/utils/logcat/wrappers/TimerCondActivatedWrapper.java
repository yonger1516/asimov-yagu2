package com.seven.asimov.it.utils.logcat.wrappers;


public class TimerCondActivatedWrapper extends LogEntryWrapper {
    private String group;
    private String script;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }


}
