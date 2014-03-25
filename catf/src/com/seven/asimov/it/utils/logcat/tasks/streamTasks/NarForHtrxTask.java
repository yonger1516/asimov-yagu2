package com.seven.asimov.it.utils.logcat.tasks.streamTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.NarForHtrxWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NarForHtrxTask extends Task<NarForHtrxWrapper> {
    private static final String TAG = NarForHtrxTask.class.getSimpleName();

    private String activatingCondition = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.*\\s.http_task.cpp:[0-9]+.\\s\\([0-9]+\\).-.Scheduling NAR for app_uid ([0-9]+) with tolerance ([0-9]+), screen trigger=([a-z]+), idle time ([0-9]+), max packet size ([0-9]+)";
    Pattern patternActivatingCondition = Pattern.compile(activatingCondition);

    @Override
    public NarForHtrxWrapper parseLine(String line) {
        Matcher matcherActivatingConditions = patternActivatingCondition.matcher(line);
        if(matcherActivatingConditions.find()){
            NarForHtrxWrapper naqForTrxWrapper = new NarForHtrxWrapper();
            setTimestampToWrapper(naqForTrxWrapper, matcherActivatingConditions);
            naqForTrxWrapper.setIdleTime(matcherActivatingConditions.group(6));
            naqForTrxWrapper.setAppUid(matcherActivatingConditions.group(3));
            return naqForTrxWrapper;
        }
        return null;
    }
}
