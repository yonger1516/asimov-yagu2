package com.seven.asimov.it.utils.logcat.wrappers;

public class PowerLogEntry extends CrcsEntry {

    private BatteryStateType state;
    private int battery_level;
    private int battery_delta;
    private int time_in_previous_state;
    private int optimization;

    public int getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(int battery_level) {
        this.battery_level = battery_level;
    }

    public int getBattery_delta() {
        return battery_delta;
    }

    public void setBattery_delta(int battery_delta) {
        this.battery_delta = battery_delta;
    }

    public int getTime_in_previous_state() {
        return time_in_previous_state;
    }

    public void setTime_in_previous_state(int time_in_previous_state) {
        this.time_in_previous_state = time_in_previous_state;
    }

    public int getOptimization() {
        return optimization;
    }

    public void setOptimization(int optimization) {
        this.optimization = optimization;
    }

    public BatteryStateType getState() {
        return state;
    }

    public void setState(BatteryStateType state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "[ID=" + getId() + " LogID=" + getLogId() + " State=" + getState()
                //+ " StateId=" + stateId
                + " BatteryLevel=" +
                battery_level + " BatteryDelta=" + battery_delta + " Time=" +
                time_in_previous_state + " Opt=" + optimization + "]";
    }
}
