package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.ReportingDumpTransactionWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportingDumpTransactionTask extends Task<ReportingDumpTransactionWrapper> {
    public static enum ReportingType {
        OUT("Sending", "request"), IN("Received", "response"), DEFAULT("\\w+", "\\w+");

        ReportingType(String param1, String param2) {
            this.param1 = param1;
            this.param2 = param2;
        }

        private String param1;
        private String param2;

        @Override
        public String toString() {
            return String.format("Parameter1: %s, parameter2: %s", param1, param2);
        }

        public String getParam1() {
            return param1;
        }

        public String getParam2() {
            return param2;
        }
    }

    private static final String TAG = ReportingDumpTransactionTask.class.getSimpleName();
    private static final String regexp7tp = "\\S+";
    private static final String regexpSizeAndHint = "\\d+";

    //    (201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* (\w+) a client reporting dump transactions (\w+) \[(\S+)->(\S+):.*\] of (\d+) bytes.*id hint (\d+)
    private static String regexp = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* (%s) a client reporting dump transactions (%s) \\[(%s)->(%s):.*\\] of (%s) bytes.*id hint (%s)";
    private Pattern pattern;

    public ReportingDumpTransactionTask(ReportingType type, String from7tp, String to7tp, String bytes, String hint) {
        regexp = String.format(regexp,
                type == null ? ReportingType.DEFAULT.getParam1() : type.getParam1(),
                type == null ? ReportingType.DEFAULT.getParam2() : type.getParam2(),
                from7tp == null ? regexp7tp : from7tp,
                to7tp == null ? regexp7tp : to7tp,
                bytes == null ? regexpSizeAndHint : bytes,
                hint == null ? regexpSizeAndHint : hint);
        pattern = Pattern.compile(regexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected ReportingDumpTransactionWrapper parseLine(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            ReportingDumpTransactionWrapper wrapper = new ReportingDumpTransactionWrapper();
            setTimestampToWrapper(wrapper, matcher);

            wrapper.setParam1(matcher.group(3));
            wrapper.setParam2(matcher.group(4));
            wrapper.setFrom7tp(matcher.group(5));
            wrapper.setTo7tp(matcher.group(6));
            wrapper.setBytes(matcher.group(7));
            wrapper.setHint(matcher.group(8));
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s2 = "12-21 11:15:13.860 D/Asimov::Java::AbstractZ7TransportMultiplexer(13327): 2013/12/21 11:15:13.866000 GMT 583 [DEBUG] [com.seven.transport.AbstractZ7TransportMultiplexer] <--- Received a client reporting dump transactions response [0-7-1->0-1f46-0:9] of 64 bytes from endpoint 0-7-1. The message has a relay id hint 1";
//        String s2 = "12-21 11:15:13.735 D/Asimov::Java::AbstractZ7TransportMultiplexer(13327): 2013/12/21 11:15:13.743000 GMT 575 [DEBUG] [com.seven.transport.AbstractZ7TransportMultiplexer] ---> Sending a client reporting dump transactions request [0-1f46-0->0-7-0:9] of 2048 bytes to endpoint 0-7-0 . The message is requesting acknowledgement. A delivery observer is observing this message (token: 507). The message has a relay id hint 0";
        ReportingDumpTransactionTask task = new ReportingDumpTransactionTask(ReportingType.IN, null, null, null, null);
        ReportingDumpTransactionWrapper wrapper = task.parseLine(s2);
        System.out.println(wrapper);
        System.out.println(wrapper.getTimestamp());
    }
}
