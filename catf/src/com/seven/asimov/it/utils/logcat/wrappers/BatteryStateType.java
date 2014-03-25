package com.seven.asimov.it.utils.logcat.wrappers;

public enum BatteryStateType {

    unknown("unknown"),
    device_on("device_on"),
    to_charger("to_charger"),
    charger("charger"),
    to_battery("to_battery"),
    battery("battery");

    private final String name;

    BatteryStateType(String name) {
        this.name = name;
    }
}