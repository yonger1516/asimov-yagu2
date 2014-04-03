package com.seven.asimov.it.utils.logcat.tasks;

import android.util.Log;
import com.seven.asimov.it.utils.date.DateUtil;
import com.seven.asimov.it.utils.date.TimeZones;
import com.seven.asimov.it.utils.logcat.LogcatUtil;
import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;

public abstract class Task<T extends LogEntryWrapper> {

    private static final Logger logger = LoggerFactory.getLogger(Task.class.getSimpleName());
    private boolean isPrintEntries = false;

    private ArrayList<T> logEntries = new ArrayList<T>();
    private long startTimestamp = System.currentTimeMillis();
    private ArrayList<T> filteredLogEntries;
    private boolean changeTimestampToGMT = true; //do we need to modify log entry's timestamp to GMT
    private LogcatUtil ownerLogcat;

    protected abstract T parseLine(String line);

    @Deprecated
    protected String getTAG() {
        return Task.class.getSimpleName();
    }

    public boolean isChangeTimestampToGMT() {
        return changeTimestampToGMT;
    }

    public void setChangeTimestampToGMT(boolean changeTimestampToGMT) {
        this.changeTimestampToGMT = changeTimestampToGMT;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public final ArrayList<T> getLogEntries() {
        notifyLogcatUtilStop();
        return getLogEntries(true);
    }

    public LogcatUtil getOwnerLogcat() {
        return ownerLogcat;
    }

    public void setOwnerLogcat(LogcatUtil ownerLogcat) {
        this.ownerLogcat = ownerLogcat;
    }

    private void notifyLogcatUtilStop() {
        if ((ownerLogcat != null) && (ownerLogcat.isRunning())) {
            logger.error("Accessing LogEntries of working logcat.");
//            try {
//                ownerLogcat.stop();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }


    public final ArrayList<T> getLogEntries(boolean updateNeeded) {
        notifyLogcatUtilStop();
        if (filteredLogEntries == null || updateNeeded) {
            filteredLogEntries = new ArrayList<T>();
            for (T logEntry : logEntries) {
                if (isPrintEntries) Log.i(getTAG(), "LogEntry:" + logEntry);
                if (logEntry.getTimestamp() >= startTimestamp) {
                    filteredLogEntries.add(logEntry);
                } else {
                    Log.v(getTAG(), "rejecting " + logEntry);
                }
            }
        }
        return filteredLogEntries;
    }

    public final ArrayList<T> getLogEntriesIgnoringTimeStamp() {
        return getLogEntriesIgnoringTimeStamp(true);
    }

    public final ArrayList<T> getLogEntriesIgnoringTimeStamp(boolean updateNeeded) {
        notifyLogcatUtilStop();
        if (filteredLogEntries == null || updateNeeded) {
            filteredLogEntries = new ArrayList<T>();
            for (T logEntry : logEntries) {
                if (isPrintEntries) Log.i(getTAG(), "LogEntry:" + logEntry);
                filteredLogEntries.add(logEntry);
            }
        }
        return filteredLogEntries;
    }

    public final ArrayList<T> getLogEntriesWoFilteringByTimeStamp() {
        notifyLogcatUtilStop();
        return logEntries;
    }

    public final LogEntryWrapper process(String line, int lineNumber) {
        T entry = parseLine(line);
        if (entry != null) {
            entry.setEntryNumber(lineNumber);
            logEntries.add(entry);
        }
        return entry;
    }

    public void setPrintEntries(boolean isPrintEntries) {
        this.isPrintEntries = isPrintEntries;
    }

    public void reset() {
        notifyLogcatUtilStop();
        logEntries.clear();
    }

    public T getEntryAfter(int entryNumber) throws Exception {
        Log.v(getTAG(), "Looking for log entry after " + entryNumber);
        for (T logEntry : getLogEntries(false)) {
            if (logEntry.getEntryNumber() > entryNumber) {
                Log.d(getTAG(), "Found " + logEntry);
                return logEntry;
            }
        }
        Log.v(getTAG(), "Log entry has not been found!!!");
        return null;
    }

    //be careful! some records can be skipped because have the equal timestamps.
    //use getEntryAfter() to get exactly the next log entry
    public T getEntryAfterTimestamp(long timeInMiliseconds) throws Exception {
        Log.v(getTAG(), "Looking for log entry after " + new Date(timeInMiliseconds));
        for (T logEntry : getLogEntries(false)) {
            if (logEntry.getTimestamp() > timeInMiliseconds) {
                Log.d(getTAG(), "Found " + logEntry);
                return logEntry;
            }
        }
        Log.v(getTAG(), "Log entry has not been found!!!");
        return null;
    }

    public String toString() {
        return toString(false);
    }

    public String toString(boolean updateNeeded) {
        StringBuilder result = new StringBuilder();
        Log.v(getTAG(), "log entries:");
        for (T logEntry : getLogEntries(updateNeeded)) {
            result.append(logEntry.toString()).append("\n");
        }
        return result.toString();
    }

    protected void setTimestampToWrapper(T wrapper, Matcher matcher, int timeIndex, int timeZoneIndex) {
        int hour = 0;
        if (changeTimestampToGMT) {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            //            logger.trace("Set timezone to GMT");
            try {
                hour = TimeZones.valueOf(matcher.group(timeZoneIndex)).getId();
            } catch (IllegalArgumentException e) {
                //.Using default timezone GMT.
                //logger.error(ExceptionUtils.getStackTrace(e));
                hour=TimeZones.valueOf("GMT").getId();
            }
        }
        long timestamp = DateUtil.format(matcher.group(timeIndex).replaceAll("/", "-")) + hour * 3600 * 1000;
        wrapper.setTimestamp(timestamp);
    }

    protected void setTimestampToWrapper(T wrapper, Matcher matcher) {
        setTimestampToWrapper(wrapper, matcher, 1, 2);
    }
}
