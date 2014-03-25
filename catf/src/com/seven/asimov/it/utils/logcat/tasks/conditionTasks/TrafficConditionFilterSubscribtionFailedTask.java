package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.wrappers.TrafficConditionFilterSubscribtionFailedWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 8/1/13
 * Time: 4:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrafficConditionFilterSubscribtionFailedTask extends Task<TrafficConditionFilterSubscribtionFailedWrapper> {
    private static final String TAG = TrafficConditionFilterSubscribtionFailedTask.class.getSimpleName();
    private String subscribtionFailedRegexp = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Failed to subscribe for filter \"%s\"";
    private Pattern subscribtionFailedPattern;

    private String filter;

    public TrafficConditionFilterSubscribtionFailedTask(String filter) {
        this.filter = filter;
        subscribtionFailedRegexp = String.format(subscribtionFailedRegexp,
                filter != null ? filter : "(.*)");
        Log.v(TAG, "subscribtionFailedRegexp=" + subscribtionFailedRegexp);
        subscribtionFailedPattern = Pattern.compile(subscribtionFailedRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected TrafficConditionFilterSubscribtionFailedWrapper parseLine(String line) {
        Matcher matcher = subscribtionFailedPattern.matcher(line);
        int groupNumber = 3;
        if (matcher.find()) {
            TrafficConditionFilterSubscribtionFailedWrapper wrapper = new TrafficConditionFilterSubscribtionFailedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setFilter(filter != null ? filter : matcher.group(groupNumber));
            //Log.d(TAG, "parseLine found line " + matcher.group(1));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

}
