package com.seven.asimov.it.utils.logcat.wrappers;

public class OCCrashWrapper extends LogEntryWrapper {
    private int pid;
    private int tid;
    private String processName;

    public OCCrashWrapper(String pid, String tid, String processName) {
        this.pid = Integer.parseInt(pid);
        this.tid = Integer.parseInt(tid);
        this.processName = processName;
    }

    public int getPid() {
        return pid;
    }

    public int getTid() {
        return tid;
    }

    public String getProcessName() {
        return processName;
    }

    @Override
    public String toString() {
        return String.format("OCEngineCrashWrapper: pid = %d, tid = %d, processName = %s;", pid, tid, processName);
    }
}