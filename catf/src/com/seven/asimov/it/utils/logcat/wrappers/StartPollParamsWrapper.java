package com.seven.asimov.it.utils.logcat.wrappers;

public class StartPollParamsWrapper extends LogEntryWrapper {

    private int pollClass;
    private int ri;
    private int it;
    private int to;
    private int tempPoll;
    private int rpRi;
    private int exceptionControl;
    private int oAuth;
    private String responseHash;

    public StartPollParamsWrapper(long timestamp, int pollClass, int ri, int it, int to, int tempPoll, int rpRi,
                                  int exceptionControl,int oAuth, String responseHash) {
        setTimestamp(timestamp);
        this.pollClass = pollClass;
        this.ri = ri;
        this.it = it;
        this.to = to;
        this.tempPoll = tempPoll;
        this.rpRi = rpRi;
        this.exceptionControl = exceptionControl;
        this.oAuth=oAuth;
        this.responseHash = responseHash;
    }


    public int getPollClass() {
        return pollClass;
    }

    public int getRi() {
        return ri;
    }

    public int getIt() {
        return it;
    }

    public int getTo() {
        return to;
    }

    public int getTempPoll() {
        return tempPoll;
    }

    public int getRpRi() {
        return rpRi;
    }

    public int getExceptionControl() {
        return exceptionControl;
    }

    public int getoAuth() {
        return oAuth;
    }

    public String getResponseHash() {
        return responseHash;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("StartPollParamsWrapper[ timestamp=");
        result.append(getTimestamp());
        result.append(",pollclass=");
        result.append(pollClass);
        result.append(",ri=");
        result.append(ri);
        result.append(",it=");
        result.append(it);
        result.append(",to=");
        result.append(to);
        result.append(",tempPoll=");
        result.append(tempPoll);
        result.append(",rpRi=");
        result.append(rpRi);
        result.append(",exceptionControl=");
        result.append(exceptionControl);
        result.append(",oAuth=");
        result.append(oAuth);
        result.append(",responseHash=");
        result.append(responseHash);
        result.append("]");
        return result.toString();
    }
}
