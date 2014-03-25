package com.seven.asimov.it.utils.sysmonitor;

public class ProcessStat {
    /*(1) The process ID*/
    private int pid;
    /*(14) Amount of time that this process has been scheduled in user mode, measured in clock ticks
      (divide by sysconf(_SC_CLK_TCK)).  This includes guest time, guest_time (time spent running a
      virtual CPU, see below), so that applications that are not aware of the guest time field do not lose
      that time from their calculations.*/
    private long uTime;
    /*(15) Amount of time that this process has been scheduled in kernel mode, measured in clock ticks (divide by sysconf(_SC_CLK_TCK)).*/
    private long sTime;

    public ProcessStat(int pid) {
        this.pid = pid;
    }

    public ProcessStat(int pid, long uTime, long sTime) {
        this.pid = pid;
        this.uTime = uTime;
        this.sTime = sTime;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public long getsTime() {
        return sTime;
    }

    public void setsTime(long sTime) {
        this.sTime = sTime;
    }

    public long getuTime() {
        return uTime;
    }

    public void setuTime(long uTime) {
        this.uTime = uTime;
    }

    @Override
    public String toString() {
        return "ProcessStat{" +
                "pid=" + pid +
                ", uTime=" + uTime +
                ", sTime=" + sTime +
                '}';
    }
}
