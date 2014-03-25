package com.seven.asimov.it.utils.logcat.wrappers;

import java.util.HashMap;
import java.util.Map;

public class DispatcherEvent extends LogEntryWrapper {

    private String timezone;
    private DISPATCHER_TYPE type;
    private DISPATHER_EVENT event;
    private int logId;

    private static Map<Integer, DISPATCHER_TYPE> types = new HashMap<Integer, DispatcherEvent.DISPATCHER_TYPE>();
    private static Map<Integer, DISPATHER_EVENT> events = new HashMap<Integer, DispatcherEvent.DISPATHER_EVENT>();
    private static Map<Integer, TIMEZONES> timezones = new HashMap<Integer, DispatcherEvent.TIMEZONES>();

    static {
        types.put(1, DISPATCHER_TYPE.HTTP);
        types.put(2, DISPATCHER_TYPE.HTTPS);
        types.put(3, DISPATCHER_TYPE.DNS);
        types.put(4, DISPATCHER_TYPE.TCP);

        events.put(1, DISPATHER_EVENT.CONNECTED);
        events.put(2, DISPATHER_EVENT.RECONNECTFAILED);

        timezones.put(1, TIMEZONES.PDT);
    }

    public DISPATCHER_TYPE getType() {
        return type;
    }

    public void setType(DISPATCHER_TYPE type) {
        this.type = type;
    }

    public int getTypeId() {
        for (int key : types.keySet()) {
            if (types.get(key) == type) {
                return key;
            }
        }
        return 0;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public DISPATHER_EVENT getEvent() {
        return event;
    }

    public void setEvent(DISPATHER_EVENT event) {
        this.event = event;
    }

    public int getEventId() {
        for (int key : events.keySet()) {
            if (events.get(key) == event) {
                return key;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "DispatcherEvent{" +
                "timezone='" + timezone + '\'' +
                ", type=" + type +
                ", event=" + event +
                ", logId=" + logId +
                '}';
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public static enum TIMEZONES {
        PDT
    }

    public static enum DISPATCHER_TYPE {
        HTTP, HTTPS, DNS, TCP
    }

    public static enum DISPATHER_EVENT {
        CONNECTED,
        RECONNECTFAILED
    }

}
