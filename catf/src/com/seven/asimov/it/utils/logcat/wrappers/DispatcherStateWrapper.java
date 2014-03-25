package com.seven.asimov.it.utils.logcat.wrappers;

public class DispatcherStateWrapper extends LogEntryWrapper{

    private static final String TAG = DispatcherStateWrapper.class.getSimpleName();
    private String name;
    private long id;
    private int pid;
    private String state;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", name=").append(name)
                .append(", id=").append(id).append(", pid=").append(pid).append(", state=").append(state).toString();
    }
}
