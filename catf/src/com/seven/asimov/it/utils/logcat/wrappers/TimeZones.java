package com.seven.asimov.it.utils.logcat.wrappers;

public enum TimeZones {
    GMT(0),
    PDT(7),
    EET(-2),
    EEST(-3),
    CAT(-2),
    CST(8),
    HKT(8);

    private int id;

    TimeZones(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static String getClassName() {
        return TimeZones.class.getName();
    }
}
