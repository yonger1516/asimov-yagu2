package com.seven.asimov.it.utils.logcat.wrappers;

public class ThreadPoolWrapper extends LogEntryWrapper {
    private int pendingTasks;

    public ThreadPoolWrapper(String pendingTasks) {
        this.pendingTasks = Integer.parseInt(pendingTasks);
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    @Override
    public String toString() {
        return "ThreadPoolWrapper{" +
                "pendingTasks='" + pendingTasks + '\'' +
                '}';
    }
}