package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.ParametrizedSystemWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedSystemTask extends Task<ParametrizedSystemWrapper> {
    private static final String TAG = ParametrizedSystemTask.class.getSimpleName();
    private static final String digitRegexp = "\\d+";
    private static final String allRegexp = ".*";
    private static String regexp = "(201[0-9]/[0-9]+/[0-9]+.[0-9]+:[0-9]+:[0-9]+.[0-9]+).([A-Z]*).*SystemLog:\\s(201[2-9]-[0-1][0-9]-[0-3][0-9].[0-2][0-9]:[0-5][0-9]:[0-5][0-9]\\.[0-9]*),system,(%s),(%s),(%s),(%s)";

    private Pattern pattern;

    public ParametrizedSystemTask() {
        this(null, null, null, null);
    }

    public ParametrizedSystemTask(String formatVersionIdRegexp, String keyRegexp, String valueRegexp, String sequenceNumberRegexp) {
        regexp = String.format(regexp,
                formatVersionIdRegexp == null ? digitRegexp : formatVersionIdRegexp,
                keyRegexp == null ? allRegexp : keyRegexp,
                valueRegexp == null ? digitRegexp : valueRegexp,
                sequenceNumberRegexp == null ? digitRegexp : sequenceNumberRegexp);

        pattern = Pattern.compile(regexp);
    }

    @Override
    public ParametrizedSystemWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ParametrizedSystemWrapper wrapper = new ParametrizedSystemWrapper();
            setTimestampToWrapper(wrapper, matcher);

            wrapper.setFormatVersionId(matcher.group(4));
            wrapper.setKey(matcher.group(5));
            wrapper.setValue(matcher.group(6));
            wrapper.setSequenceNumber(matcher.group(7));
            return wrapper;
        }
        return null;
    }

    public static void main(String... args) {
        String s2 = "12-22 11:16:56.220 D/Asimov::Native::OCEngine(17482): 2013/12/22 11:16:56.225601 GMT 17496 [DEBUG]\t[report_service.cpp:374] (0) - SystemLog: 2013-12-22 11:16:56.225,system,2,backlight,100,1";
        ParametrizedSystemTask task = new ParametrizedSystemTask(null, null, null, null);
        ParametrizedSystemWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
