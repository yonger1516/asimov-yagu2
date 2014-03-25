package com.seven.asimov.it.utils.sysmonitor;

public class AppInfo {
    private long time;
    private float cpuTotal;
    private float availableMemory;
    private float totalMemory;
    private int totalRAM;
    private int usedRAM;

    public AppInfo() {
    }

    public AppInfo(long time, float availableMemory, float totalMemory, float cpuTotal, int usedRAM, int totalRAM) {
        this.availableMemory = availableMemory;
        this.cpuTotal = cpuTotal;
        this.time = time;
        this.totalMemory = totalMemory;
        this.totalRAM = totalRAM;
        this.usedRAM = usedRAM;
    }

    public float getAvailableMemory() {
        return availableMemory;
    }

    public void setAvailableMemory(float availableMemory) {
        this.availableMemory = availableMemory;
    }

    public float getCpuTotal() {
        return cpuTotal;
    }

    public void setCpuTotal(float cpuTotal) {
        this.cpuTotal = cpuTotal;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public float getTotalMemory() {
        return totalMemory;
    }

    public void setTotalMemory(float totalMemory) {
        this.totalMemory = totalMemory;
    }

    public int getTotalRAM() {
        return totalRAM;
    }

    public void setTotalRAM(int totalRAM) {
        this.totalRAM = totalRAM;
    }

    public int getUsedRAM() {
        return usedRAM;
    }

    public void setUsedRAM(int usedRAM) {
        this.usedRAM = usedRAM;
    }
}
