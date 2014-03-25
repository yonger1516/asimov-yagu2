package com.seven.asimov.it.utils.logcat.wrappers;


public class ScriptLogWrapper extends LogEntryWrapper {
    private String logTime;
    private String transactType;
    private int version;
    private String appName;
    private String scriptName;
    private int state;
    private int event;
    private int errorCode;
    private String eventData;
    private long sequenceNumber;

    public enum STATES{
        SCRIPT_STATE_NOT_INITIALIZED(0),
        SCRIPT_STATE_DISABLED(1),
        SCRIPT_STATE_WAITING(2),
        SCRIPT_STATE_EXITED(3),
        SCRIPT_STATE_ENTERED(4);

        private int value;

        STATES(int value){
            this.value=value;
        }

        public int toInteger(){
            return value;
        }
    }

    public enum EVENTS{
        SCRIPT_EVENT_INITIALIZED(1),
        SCRIPT_EVENT_DESTROYED(2),
        SCRIPT_EVENT_STATE_SWITCHED(3),
        SCRIPT_EVENT_ERROR_OCCURRED(4);

        private int value;

        EVENTS(int value){
            this.value=value;
        }

        public int toInteger(){
            return value;
        }
    }


    public String getLogTime() {
        return logTime;
    }

    public void setLogTime(String logTime) {
        this.logTime = logTime;
    }

    public String getTransactType() {
        return transactType;
    }

    public void setTransactType(String transactType) {
        this.transactType = transactType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getEventData() {
        return eventData;
    }

    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public String toString() {
       return "ScriptLogWrapper(" +
                " ,timestamp="+getTimestamp()+
                " ,logTime="+logTime+
                " ,transactType="+transactType+
                " ,version="+version+
                " ,appName="+appName+
                " ,scriptName="+scriptName+
                " ,state="+state+
                " ,event="+event+
                " ,errorCode="+errorCode+
                " ,eventData="+eventData+
                " ,sequenceNumber="+sequenceNumber+")";
    }
}
