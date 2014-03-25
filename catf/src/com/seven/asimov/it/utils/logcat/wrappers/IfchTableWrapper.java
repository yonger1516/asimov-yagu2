package com.seven.asimov.it.utils.logcat.wrappers;

public class IfchTableWrapper extends LogEntryWrapper{

       private String interfaceType;

       public void setInterfaceType(String interfaceType){
        this.interfaceType = interfaceType;
    }

    public String getInterfaceType(){
        return interfaceType;
    }

    @Override
    public String toString() {
        return "IfchTableWrapper{" +
                "interfaceType='" + interfaceType + '\'' +
                '}';
    }
}
