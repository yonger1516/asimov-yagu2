package com.seven.asimov.it.utils.sysmonitor;

public class CpuStat {

    /*(1) Time spent in user mode*/
    private long userTime;

    /*(2) Time spent in user mode with low priority*/
    private long niceTime;

    /*(3) Time spent in system mode*/
    private long systemTime;

    /*(4) Time spent in the idle task.  This value should be USER_HZ times the second entry in the
                            /proc/uptime pseudo-file.*/
    private long idleTime;

    /*(5) Time waiting for I/O to complete*/
    private long ioWaitTime;

    /*(6) Time servicing interrupts*/
    private long irqTime;

    /*(7) Time servicing softirqs*/
    private long softIrqTime;

    /*(8) Stolen time, which is the time spent in other operating systems when running in a virtualized environment*/
    private long stealTime;

    /*(9) Time spent running a virtual CPU for guest operating systems under the control of the Linux kernel.*/
    private long guestTime;

    /*(10) Time spent running a niced guest (virtual CPU for guest operating systems under the control of the Linux kernel)*/
    private long guestNiceTime;

    /*Total time for calculation total cpu usage*/
    private long totalTime;

    public CpuStat(long userTime, long niceTime, long systemTime, long idleTime, long ioWaitTime, long irqTime,
                   long softIrqTime, long stealTime, long guestTime, long guestNiceTime) {
        this.userTime = userTime;
        this.niceTime = niceTime;
        this.systemTime = systemTime;
        this.idleTime = idleTime;
        this.ioWaitTime = ioWaitTime;
        this.irqTime = irqTime;
        this.softIrqTime = softIrqTime;
        this.stealTime = stealTime;
        this.guestTime = guestTime;
        this.guestNiceTime = guestNiceTime;
    }

    public long getGuestNiceTime() {
        return guestNiceTime;
    }

    public void setGuestNiceTime(long guestNiceTime) {
        this.guestNiceTime = guestNiceTime;
    }

    public long getGuestTime() {
        return guestTime;
    }

    public void setGuestTime(long guestTime) {
        this.guestTime = guestTime;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public long getIoWaitTime() {
        return ioWaitTime;
    }

    public void setIoWaitTime(long ioWaitTime) {
        this.ioWaitTime = ioWaitTime;
    }

    public long getIrqTime() {
        return irqTime;
    }

    public void setIrqTime(long irqTime) {
        this.irqTime = irqTime;
    }

    public long getNiceTime() {
        return niceTime;
    }

    public void setNiceTime(long niceTime) {
        this.niceTime = niceTime;
    }

    public long getSoftIrqTime() {
        return softIrqTime;
    }

    public void setSoftIrqTime(long softIrqTime) {
        this.softIrqTime = softIrqTime;
    }

    public long getStealTime() {
        return stealTime;
    }

    public void setStealTime(long stealTime) {
        this.stealTime = stealTime;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public long getUserTime() {
        return userTime;
    }

    public void setUserTime(long userTime) {
        this.userTime = userTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("CpuStat{")
                .append("userTime=").append(userTime)
                .append(", niceTime=").append(niceTime)
                .append(", systemTime=").append(systemTime)
                .append(", idleTime=").append(idleTime)
                .append(", ioWaitTime=").append(ioWaitTime)
                .append(", irqTime=").append(irqTime)
                .append(", softIrqTime=").append(softIrqTime)
                .append(", stealTime=").append(stealTime)
                .append(", guestTime=").append(guestTime)
                .append("guestNiceTime=").append(guestNiceTime)
                .append(", totalTime=").append(totalTime)
                .append('}').toString();
    }
}
