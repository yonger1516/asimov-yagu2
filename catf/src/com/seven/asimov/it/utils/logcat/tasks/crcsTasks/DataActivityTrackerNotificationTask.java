package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;


import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DataActivityTrackerNotificationWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataActivityTrackerNotificationTask extends Task<DataActivityTrackerNotificationWrapper> {

    private static final String TAG = DataActivityTrackerNotificationTask.class.getSimpleName();

    private String DataActivityTrackerNotificationRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* notify about - ([0-9]:)%s.*";
    private Pattern DataActivityTrackerNotificationPattern;
    private String notification;

    public DataActivityTrackerNotificationTask(String notification) {
        DataActivityTrackerNotificationRegexp = String.format(DataActivityTrackerNotificationRegexp, notification);
        DataActivityTrackerNotificationPattern = Pattern.compile(DataActivityTrackerNotificationRegexp, Pattern.CASE_INSENSITIVE);
        this.notification = notification;
        Log.v(TAG, "DataActivityTrackerNotificationRegexp= " + DataActivityTrackerNotificationRegexp);
    }

    @Override
    protected DataActivityTrackerNotificationWrapper parseLine(String line) {
        Matcher matcher = DataActivityTrackerNotificationPattern.matcher(line);
        if (matcher.find()) {
            DataActivityTrackerNotificationWrapper wrapper = new DataActivityTrackerNotificationWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setNotification(this.notification);

            return wrapper;
        }

        return null;
    }
}
