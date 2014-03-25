package com.seven.asimov.it.utils.logcat.tasks.pollingTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CheckUpdatePolligWrapper;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckUpdatePollingTask extends Task<CheckUpdatePolligWrapper> {
    private static final String TAG = CheckUpdatePollingTask.class.getSimpleName();

    private static final String CUP_REGEXP1 =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*com.seven.asimov.update.poll.PollingUpgradeCheckService.*isPollingEnabled = ([a-z]*), isCheckPending = ([a-z]*), hasNetworkConnectivity = ([a-z]*), shouldWaitRoamingStop = ([a-z]*)";
    private static Pattern cupPattern1 = Pattern.compile(CUP_REGEXP1, Pattern.CASE_INSENSITIVE);

    protected CheckUpdatePolligWrapper parseLine(String line) {
        CheckUpdatePolligWrapper wrapper = new CheckUpdatePolligWrapper();
        Matcher matcher1 = cupPattern1.matcher(line);

        if (matcher1.find()) {
            setTimestampToWrapper(wrapper, matcher1);
            wrapper.setPollingEnabled(matcher1.group(3));
            wrapper.setCheckPending(matcher1.group(4));
            wrapper.setNetworkConnectivity(matcher1.group(5));
            wrapper.setWaitRoamingStop(matcher1.group(6));

            return wrapper;
        }
        return null;
    }
}
