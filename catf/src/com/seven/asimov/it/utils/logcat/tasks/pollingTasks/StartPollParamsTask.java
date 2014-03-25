package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.StartPollParamsWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StartPollParamsTask extends Task<StartPollParamsWrapper> {

    private static final String START_POLL_PARAMS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*" +
                    "Start poll parameters: package com.seven.asimov.it, poll class: ([\\d\\-]+), " +
                    "RI: ([\\d\\-]+), IT: ([\\d\\-]+), TO: ([\\d\\-]+), temp poll: ([\\d\\-]+), " +
                    "RP RI: ([\\d\\-]+), exception control: ([\\d\\-]+), OAuth: ([\\d\\-]+), " +
                    "response hash: \\[([\\d\\w]+)\\]";
    private static final Pattern startPollParamsPattern = Pattern.compile(START_POLL_PARAMS_REGEXP);

    @Override
    protected StartPollParamsWrapper parseLine(String line) {
        Matcher matcher = startPollParamsPattern.matcher(line);
        if (matcher.find()) {
            StartPollParamsWrapper wrapper = new StartPollParamsWrapper(0,
                    Integer.valueOf(matcher.group(3)), Integer.valueOf(matcher.group(4)), Integer.valueOf(matcher.group(5)),
                    Integer.valueOf(matcher.group(6)),Integer.valueOf(matcher.group(7)),
                    Integer.valueOf(matcher.group(8)),Integer.valueOf(matcher.group(9)),
                    Integer.valueOf(matcher.group(10)),matcher.group(11));
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s = "04-22 00:34:34.488: V/Asimov::JNI::OCEngine(1226): 2013/04/22 0:34:34.491867 EEST 1460 [FTRACE]\t[subscription_manager.cpp:544] (0) - Start poll parameters: package com.seven.asimov.it, poll class: 1, RI: 38, IT: 0, TO: 0, temp poll: 0, RP RI: 38, exception control: 0, OAuth: 0, response hash: [4D6807E52C345DBD62067E6D71ECBE06]\n";
        StartPollParamsTask task = new StartPollParamsTask();
        StartPollParamsWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
    }
}
