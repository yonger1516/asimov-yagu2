package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.ParametrizedPowerWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedPowerTask extends Task<ParametrizedPowerWrapper> {
    private static final String TAG = ParametrizedPowerTask.class.getSimpleName();
    private static final String digitRegexp = "\\d+";
    private static final String allRegexp = ".*";
    private static String regexp = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).*PowerLog:\\s(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),power,(%s),(%s),(%s),(%s),(%s),(%s),(%s)";

    private Pattern pattern;

    public ParametrizedPowerTask() {
        this(null, null, null, null, null, null, null);
    }

    public ParametrizedPowerTask(String formatVersionIdRegexp, String eventRegexp, String levelRegexp, String deltaRegexp, String intervalRegexp, String optimizationRegexp, String sequenceNumber) {
        regexp = String.format(regexp,
                formatVersionIdRegexp == null ? digitRegexp : formatVersionIdRegexp,
                eventRegexp == null ? allRegexp : eventRegexp,
                levelRegexp == null ? allRegexp : levelRegexp,
                deltaRegexp == null ? digitRegexp : deltaRegexp,
                intervalRegexp == null ? digitRegexp : intervalRegexp,
                optimizationRegexp == null ? allRegexp : optimizationRegexp,
                sequenceNumber == null ? allRegexp : sequenceNumber);
        pattern = Pattern.compile(regexp);
    }

    @Override
    public ParametrizedPowerWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ParametrizedPowerWrapper wrapper = new ParametrizedPowerWrapper();
            setTimestampToWrapper(wrapper, matcher);

            wrapper.setFormatVersionId(matcher.group(4));
            wrapper.setEvent(matcher.group(5));
            wrapper.setLevel(matcher.group(6));
            wrapper.setDelta(matcher.group(7));
            wrapper.setInterval(matcher.group(8));
            wrapper.setOptimization(matcher.group(9));
            wrapper.setSequenceNumber(matcher.group(10));
            return wrapper;
        }
        return null;
    }

    public static void main(String... args) {
        String s2 = "12-22 11:16:56.075 D/Asimov::Native::OCEngine(17482): 2013/12/22 11:16:56.079253 GMT 17482 [DEBUG]\t[report_service.cpp:319] (0) - PowerLog: 2013-12-22 11:16:56.078,power,3,charger,100,0,0,1,1";
        ParametrizedPowerTask task = new ParametrizedPowerTask(null, null, null, null, null, null, null);
        ParametrizedPowerWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
