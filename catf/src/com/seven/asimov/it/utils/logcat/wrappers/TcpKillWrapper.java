package com.seven.asimov.it.utils.logcat.wrappers;

public class TcpKillWrapper extends LogEntryWrapper {
    private int PID;

    public int getPID() {
        return PID;
    }

    public void setPID(int tcpKillPID) {
        this.PID = tcpKillPID;
    }
}
