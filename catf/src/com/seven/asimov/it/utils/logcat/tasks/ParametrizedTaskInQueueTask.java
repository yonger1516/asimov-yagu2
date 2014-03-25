package com.seven.asimov.it.utils.logcat.tasks;

import com.seven.asimov.it.utils.logcat.wrappers.ParametrizedTaskInQueueWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParametrizedTaskInQueueTask extends Task<ParametrizedTaskInQueueWrapper> {
    private static final String TAG = ParametrizedTaskInQueueTask.class.getSimpleName();
    private static final String capitalLettersRegexp = "[A-Z_]*";
    private static final String digitsRegexp = "[0-9]*";

    private static String regexp = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*Z7Task \\[status=(%s).*type=(%s).*token=(%s)\\] added to the queue";
    private Pattern pattern;

    public ParametrizedTaskInQueueTask() {
        this(null, null, null);
    }

    public ParametrizedTaskInQueueTask(String statusRegexp, String typeRegexp){
        this(statusRegexp, typeRegexp, null);
    }

    public ParametrizedTaskInQueueTask(String statusRegexp, String typeRegexp, String tokenRegexp) {
        regexp = String.format(regexp,
                statusRegexp == null ? capitalLettersRegexp : statusRegexp,
                typeRegexp == null ? capitalLettersRegexp : typeRegexp,
                tokenRegexp == null ? digitsRegexp : tokenRegexp);

        pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public ParametrizedTaskInQueueWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ParametrizedTaskInQueueWrapper wrapper = new ParametrizedTaskInQueueWrapper();
            setTimestampToWrapper(wrapper, matcher);

            wrapper.setStatus(matcher.group(3));
            wrapper.setType(matcher.group(4));
            wrapper.setToken(matcher.group(5));
            return wrapper;
        }
        return null;
    }

    public static void main(String... args) {
//        String s2 = "01-03 17:50:10.565 D/Asimov::Java::Z7TaskManager(15153): 2014/01/03 17:50:10.569000 GMT 1108 [DEBUG] [com.seven.client.core.task.Z7TaskManager] Z7Task [status=NEW, type=REPORT_TRANSFER, priority=0, radioUp=false, token=505] added to the queue";
        String s2 = "01-07 01:37:12.620 D/Asimov::Java::Z7TaskManager( 5387): 2014/01/07 01:37:12.625000 GMT 5397 [DEBUG] [com.seven.client.core.task.Z7TaskManager] Z7Task [status=NEW, type=REPORT_TRANSFER, priority=0, radioUp=true, token=968] added to the queue";
        ParametrizedTaskInQueueTask task = new ParametrizedTaskInQueueTask("NEW", "REPORT_TRANSFER");
        ParametrizedTaskInQueueWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
