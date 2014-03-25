package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.FTMdropMessageWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 10/12/13
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class FTMdropMessageTask extends Task<FTMdropMessageWrapper> {
    private static final String TAG = FTMdropMessageTask.class.getSimpleName();

    private static final String FTM_DROP_MESSAGE_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).* Going to send ([0-9]*) \"drop\" FTM messages";
    private static final Pattern ftmDropMessagePattern = Pattern.compile(FTM_DROP_MESSAGE_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public FTMdropMessageWrapper parseLine(String line) {
        Matcher matcher = ftmDropMessagePattern.matcher(line);
        if (matcher.find()) {
            FTMdropMessageWrapper wrapper = new FTMdropMessageWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setCount(Integer.parseInt(matcher.group(3)));
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