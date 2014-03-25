package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.LogcatChecks;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.RadioLogEntry;
import com.seven.asimov.it.utils.logcat.wrappers.RadioStateType;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadiologTask extends Task<RadioLogEntry> {
    private static final String TAG = RadiologTask.class.getSimpleName();

    private final static String RADIOLOG_REGEXP_1 = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).*" +
            "(RadioLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),(radio,.),([_a-zA-Z]+),([_a-zA-Z]+),([-0-9]+),([-0-9]+)";
    private final static String RADIOLOG_REGEXP_2 = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).*" +
            "(RadioLog\\:).(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),(radio,.),([_a-zA-Z]+),([_a-zA-Z]+),([-0-9]+)";


    private final static Pattern radiologPattern1 = Pattern.compile(RADIOLOG_REGEXP_1, Pattern.CASE_INSENSITIVE);
    private final static Pattern radiologPattern2 = Pattern.compile(RADIOLOG_REGEXP_2, Pattern.CASE_INSENSITIVE);

    @Override
    protected RadioLogEntry parseLine(String line) {
        Matcher m1 = radiologPattern1.matcher(line);
        Matcher m2 = radiologPattern2.matcher(line);
        if (m1.find()) {
            RadioLogEntry result = new RadioLogEntry();
            setTimestampToWrapper(result, m1);
            result.setTimeInCurrentState(LogcatChecks.getUnixTimeFromString(m1.group(4)));
            try {
                result.setCurrentState(RadioStateType.valueOf(m1.group(6)));
            } catch (IllegalArgumentException e) {
                result.setCurrentState(RadioStateType.unknown);
            }
            try {
                result.setPreviousState(RadioStateType.valueOf(m1.group(7)));
            } catch (IllegalArgumentException e) {
                result.setPreviousState(RadioStateType.unknown);
            }
            result.setTimeInPreviousState(Integer.parseInt(m1.group(8)));
            result.setOptimization(Integer.parseInt(m1.group(9)));
            return result;
        } else if (m2.find()) {
            RadioLogEntry result = new RadioLogEntry();
            setTimestampToWrapper(result, m2);
            result.setTimeInCurrentState(LogcatChecks.getUnixTimeFromString(m2.group(4)));
            try {
                result.setCurrentState(RadioStateType.valueOf(m2.group(6)));
            } catch (IllegalArgumentException e) {
                result.setCurrentState(RadioStateType.unknown);
            }
            try {
                result.setPreviousState(RadioStateType.valueOf(m2.group(7)));
            } catch (IllegalArgumentException e) {
                result.setPreviousState(RadioStateType.unknown);
            }
            try {
                result.setTimeInPreviousState(Integer.parseInt(m2.group(8)));
            } catch (NumberFormatException e) {
                Log.e(TAG, ExceptionUtils.getStackTrace(e));
                result.setTimeInPreviousState(-1);
            }
            result.setOptimization(-1);
            return result;
        }
        return null;
    }
}
