package com.seven.asimov.it.utils.sysmonitor;

import java.util.ArrayList;
import java.util.List;

public class AppInfoOC extends AppInfo {
    private float cpuController;
    private float cpuEngine;
    private int memController;
    private int memEngine;
    private List<DispatcherInfo> dispatcherList = new ArrayList<DispatcherInfo>();

    public AppInfoOC() {
    }

    public AppInfoOC(long time, float availableMemory, float totalMemory, float cpuTotal, int usedRAM, int totalRAM,
                     float cpuController, float cpuEngine, int memController, int memEngine) {
        super(time, availableMemory, totalMemory, cpuTotal, usedRAM, totalRAM);
        this.cpuController = cpuController;
        this.cpuEngine = cpuEngine;
        this.memController = memController;
        this.memEngine = memEngine;
    }

    public float getCpuController() {
        return cpuController;
    }

    public void setCpuController(float cpuController) {
        this.cpuController = cpuController;
    }

    public float getCpuEngine() {
        return cpuEngine;
    }

    public void setCpuEngine(float cpuEngine) {
        this.cpuEngine = cpuEngine;
    }

    public int getMemController() {
        return memController;
    }

    public void setMemController(int memController) {
        this.memController = memController;
    }

    public int getMemEngine() {
        return memEngine;
    }

    public void setMemEngine(int memEngine) {
        this.memEngine = memEngine;
    }

    public List<DispatcherInfo> getDispatcherList() {
        return dispatcherList;
    }

    public void setDispatcherList(List<DispatcherInfo> dispatcherList) {
        this.dispatcherList = dispatcherList;
    }

    public void addDispatcher(DispatcherInfo dispatcherInfo) {
        this.dispatcherList.add(dispatcherInfo);
    }
}
