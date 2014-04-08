package com.seven.asimov.it.utils.date;

import android.app.AlarmManager;
import android.content.Context;
import android.util.Log;
import com.seven.asimov.it.base.AsimovTestCase;
import com.seven.asimov.it.base.HttpRequest;
import com.seven.asimov.it.base.HttpResponse;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtil {
    private static final TimeZone INITIAL_DEVICE_TIME_ZONE = TimeZone.getDefault();
    public static final TimeZone GMT_TZ = TimeZone.getTimeZone("GMT");
    public static final TimeZone EET_TZ = TimeZone.getTimeZone("GMT+3:00");

    public static final String RFC1123_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
    public static final String RFC850_DATE_FORMAT = "EEEE, dd-MMM-yyyy HH:mm:ss z";
    public static final String ASCTIME_DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy";
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final long SECONDS = 1000l;
    public static final long MINUTES = 60 * SECONDS;
    public static final long HOURS = 60 * MINUTES;
    public static final long DAYS = 24 * HOURS;

    public static final long CURRENT_DEVICE_TZ_OFFSET = TimeZone.getDefault().getOffset(Calendar.ZONE_OFFSET);
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class.getSimpleName());
    private static TimeZone currentTimeZone = INITIAL_DEVICE_TIME_ZONE;
    private static final String resource = "asimov_time_sync";

    public static void setTimeZoneOnDevice(Context context, TimeZone tz) throws IOException {
        AlarmManager alManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        logger.info(String.format("Setting time zone: %s", tz));
        alManager.setTimeZone(tz.getID());
        currentTimeZone = tz;

        if (!tz.equals(TimeZone.getDefault())){
             logger.error("Set time zone failed.");
             throw new IOException("Set time zone failed.");
        }
    }

    public static void syncTimeWithTestRunner() {
        String uri = AsimovTestCase.createTestResourceUri(resource);
        HttpRequest request = AsimovTestCase.createRequest().setUri(uri).setMethod("GET")
                .addHeaderField("X-OC-AddHeaderOnRequest_Date", "GMT").getRequest();
        HttpResponse response = null;
        try {
            response = AsimovTestCase.sendRequest(request, false, true);
            if (response != null) {
                String date = response.getHeaderField("Date");
                if (date != null) {
                    DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                    setTimeOnDevice(dateFormat.parse(date).getTime());
                } else {
                    logger.info("Sync is failed: header \"Date\" does not exist");
                }
            } else {
                logger.info("Sync is failed: response does not exist");
            }
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            logger.error("Unable to set time on your device");
        }
    }

    public static void moveTime(long milliseconds) {
        logger.info(String.format("Moving time: %d", milliseconds));
        setTimeOnDevice(System.currentTimeMillis() + milliseconds);
    }

    public static void resetDeviceTimeZoneToDefault(Context context) {
        logger.info(String.format("Resetting timezone to initial: %s", INITIAL_DEVICE_TIME_ZONE.getID()));
        AlarmManager alManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alManager.setTimeZone(INITIAL_DEVICE_TIME_ZONE.getID());
        currentTimeZone = INITIAL_DEVICE_TIME_ZONE;
    }

    private static String formatDate(long timestamp) {
        StringBuilder result = new StringBuilder();
        Calendar cal = Calendar.getInstance(currentTimeZone);
        cal.setTimeInMillis(timestamp);
        appendPart(result, cal.get(Calendar.YEAR));
        appendPart(result, cal.get(Calendar.MONTH) + 1);
        appendPart(result, cal.get(Calendar.DAY_OF_MONTH));
        result.append(".");
        appendPart(result, cal.get(Calendar.HOUR_OF_DAY));
        appendPart(result, cal.get(Calendar.MINUTE));
        appendPart(result, cal.get(Calendar.SECOND));
        logger.info(String.format("Final timestamp to set: %s", result.toString()));
        return result.toString();
    }

    private static void appendPart(StringBuilder sb, int numb) {
        if (numb < 10) {
            sb.append(0);
        }
        sb.append(numb);
    }

    public static void setTimeOnDevice(long millis) {
        try {
            logger.info(String.format("Trying to format: %d", millis));
            String[] a = {"su", "-c", String.format("date -s %s", formatDate(millis))};
            Runtime.getRuntime().exec(a).waitFor();
        } catch (Exception e) {
            logger.error("Failed to set system time. " + /*ExceptionUtils.getStackTrace(e)*/ e.getMessage());
        }
    }

    public static String format(Date d) {
        return format(d, DateUtil.GMT_TZ);
    }

    public static String format(Date d, TimeZone tz) {
        return format(d, tz, Locale.ENGLISH, DateUtil.RFC1123_DATE_FORMAT);
    }

    public static String format(Date d, String format) {
        return format(d, DateUtil.GMT_TZ, Locale.ENGLISH, format);
    }

    public static String format(Date d, Locale l) {
        return format(d, DateUtil.GMT_TZ, l, DateUtil.RFC1123_DATE_FORMAT);
    }

    public static String format(Date d, TimeZone tz, Locale l, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, l);
        simpleDateFormat.setTimeZone(tz);
        String formattedDate = simpleDateFormat.format(d);
        int idx = formattedDate.indexOf("+00:00");
        if (idx > -1) {
            formattedDate = formattedDate.replace("+00:00", "");
        }
        return formattedDate;
    }

    // Actual ASCTIME format has two SP after month if date is 1 digit
    public static String formatAscTime(Date d) {
        String date = format(d, ASCTIME_DATE_FORMAT);
        if (!Character.isDigit(date.charAt(9))) {
            StringBuffer sBuff = new StringBuffer(date);
            sBuff.insert(7, ' ');
            date = sBuff.toString();
        }
        return date;
    }

    // For TestFramework parsing tasks which must find time in SIMPLE_DATE_FORMAT
    public static long format(String time) {
        SimpleDateFormat df = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
        Date date = df.parse(time, new ParsePosition(0));
        date.setTime(date.getTime() + (Long.parseLong(time.substring(time.indexOf(".") + 1))) / 1000);
        return date.getTime();
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.format(new Date(System.currentTimeMillis())));
    }
}
