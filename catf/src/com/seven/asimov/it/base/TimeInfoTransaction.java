package com.seven.asimov.it.base;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 1/30/14
 * Time: 4:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeInfoTransaction {

    private long timeStart;
    private long timeEnd;

    public TimeInfoTransaction() {
    }

    public TimeInfoTransaction(long start, long end) {
        timeStart = start;
        timeEnd = end;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getResponseTime() {
        return (int) ((timeEnd - timeStart) / 1000);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("StartTime = ").append(timeStart).append(" EndTime = ").append(timeEnd);
        return result.toString();
    }
}
