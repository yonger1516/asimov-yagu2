package com.seven.asimov.it.utils.logcat.tasks;


import com.seven.asimov.it.utils.logcat.wrappers.ParametrizedTrafficWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedTrafficTask extends Task<ParametrizedTrafficWrapper> {

    private static final String TAG = ParametrizedTrafficTask.class.getSimpleName();
    private static final String digitRegexp = "\\d+";
    private static final String allRegexp = ".*";
    private static String regexp = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).*TrafficLog:\\s(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),traffic,(%s),(%s),(%s),(%s),(%s),(%s)";

    private Pattern pattern;

    public ParametrizedTrafficTask() {
        this(null, null, null, null, null, null);
    }

    public ParametrizedTrafficTask(String formatVersionIdRegexp, String trafficTypeRegexp, String inRegexp, String outRegexp, String optimizationRegexp, String sequenceNumber) {
        regexp = String.format(regexp,
                formatVersionIdRegexp == null ? digitRegexp : formatVersionIdRegexp,
                trafficTypeRegexp == null ? allRegexp : trafficTypeRegexp,
                inRegexp == null ? allRegexp : inRegexp,
                outRegexp == null ? digitRegexp : outRegexp,
                optimizationRegexp == null ? allRegexp : optimizationRegexp,
                sequenceNumber == null ? allRegexp : sequenceNumber);
        pattern = Pattern.compile(regexp);
    }

    @Override
    public ParametrizedTrafficWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ParametrizedTrafficWrapper wrapper = new ParametrizedTrafficWrapper();
            setTimestampToWrapper(wrapper, matcher);

            wrapper.setFormatVersionId(matcher.group(4));
            wrapper.setTrafficType(matcher.group(5));
            wrapper.setIn(matcher.group(6));
            wrapper.setOut(matcher.group(7));
            wrapper.setOptimization(matcher.group(8));
            wrapper.setSequenceNumber(matcher.group(9));
            return wrapper;
        }
        return null;
    }

    public static void main(String... args) {
        String s2 = "12-22 11:21:56.060 D/Asimov::Native::OCEngine(17482): 2013/12/22 11:21:56.065189 GMT 17525 [DEBUG]\t[report_service.cpp:335] (0) - TrafficLog: 2013-12-22 11:21:56.064,traffic,3,total,154098,51448,1,0";
        ParametrizedTrafficTask task = new ParametrizedTrafficTask(null, null, null, null, null, null);
        ParametrizedTrafficWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
