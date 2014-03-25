package com.seven.asimov.it.utils;

import android.util.Log;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Calendar;

public class TimeSetterUtil {

    private static final String TAG = TimeSetterUtil.class.getSimpleName();
    public static final long MS_IN_DAY = 86400000L; //1000 * 3600 * 24

    private TimeSetterUtil() {
    }

    public static TimeSetterUtil getInstance() {
        if (instance == null) {
            instance = new TimeSetterUtil();
        }
        return instance;
    }

    private static TimeSetterUtil instance;
    private long timeDiff;
    private boolean isTimeChanged = false;

    public void setEmulatorsTime(long diff) {
        if (!isTimeChanged) {
            timeDiff = diff;
            setSystemTime(System.currentTimeMillis() + timeDiff);
            isTimeChanged = true;
        }
    }

    public void setEmulatorsTimeBack() {
        if (isTimeChanged) {
            setSystemTime(System.currentTimeMillis() - timeDiff);
            isTimeChanged = false;
        }
    }

    private static String formatDate(long timestamp) {
        StringBuilder result = new StringBuilder();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        appendPart(result, cal.get(Calendar.YEAR));
        appendPart(result, cal.get(Calendar.MONTH) + 1);
        appendPart(result, cal.get(Calendar.DAY_OF_MONTH));
        result.append(".");
        appendPart(result, cal.get(Calendar.HOUR_OF_DAY));
        appendPart(result, cal.get(Calendar.MINUTE));
        appendPart(result, cal.get(Calendar.SECOND));

        return result.toString();
    }

    private static void appendPart(StringBuilder sb, int numb) {
        if (numb < 10) {
            sb.append(0);
        }
        sb.append(numb);
    }

    public static void setSystemTime(long ts) {
        try {
            String[] a = { "su", "-c", String.format("date -s %s", formatDate(ts)) };
            Runtime.getRuntime().exec(a);
            Thread.sleep(60000);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set system time. " + ExceptionUtils.getStackTrace(e));
        }
    }

    public static void setSystemTimeAndWaitFor(long ts) {
        try {
            String[] a = { "su", "-c", String.format("date -s %s", formatDate(ts)) };
            Runtime.getRuntime().exec(a).waitFor();
        } catch (Exception e) {
            Log.e(TAG, "Failed to set system time. " + ExceptionUtils.getStackTrace(e));
        }
    }

}