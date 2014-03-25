package com.seven.asimov.it.utils.sysmonitor;

public class DispatcherInfo {
    private String name;
    private float cpuUsage;
    private int memoryUsage;

    public DispatcherInfo() {
    }

    public DispatcherInfo(String name, float cpuUsage, int memoryUsage) {
        this.name = name;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
    }

    public float getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public int getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(int memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
