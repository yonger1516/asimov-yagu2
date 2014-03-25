package com.seven.asimov.it.utils.logcat.wrappers;


public class ServiceLogWrapper extends LogEntryWrapper {
  private String type;
  private String action;
  private String extra1;
  private String extra2;

    @Override
    public String toString() {
        return "ServiceLogWrapper{" +
                "type='" + type + '\'' +
                ", action='" + action + '\'' +
                ", extra1='" + extra1 + '\'' +
                ", extra2='" + extra2 + '\'' +
                '}';
    }

    public String getExtra2() {
        return extra2;
    }

    public void setExtra2(String extra2) {
        this.extra2 = extra2;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExtra1() {
        return extra1;
    }

    public void setExtra1(String extra1) {
        this.extra1 = extra1;
    }


}
