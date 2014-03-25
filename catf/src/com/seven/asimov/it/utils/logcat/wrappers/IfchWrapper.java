package com.seven.asimov.it.utils.logcat.wrappers;

public class IfchWrapper extends LogEntryWrapper {

      private String interfaceType;
    private double timeAfterStartAndroid;


    public void setInterfaceType(String interfaceType){
        this.interfaceType = interfaceType;
    }

    public String getInterfaceType(){
        return interfaceType;
    }

    public double getTimeAfterStartAndroid() {
        return timeAfterStartAndroid;
    }

    public void setTimeAfterStartAndroid(double timeAfterStartAndroid) {
        this.timeAfterStartAndroid = timeAfterStartAndroid;
    }

    @Override
    public String toString() {
        return "IfchWrapper{" +
                "interfaceType='" + interfaceType + '\'' +
                ", timeAfterStartAndroid=" + timeAfterStartAndroid +
                '}';
    }
}
