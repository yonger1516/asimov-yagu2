package com.seven.asimov.it.utils.logcat.tasks.streamTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.NaqForTrxWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaqForTrxTask extends Task<NaqForTrxWrapper> {
    private static final String TAG = NaqForTrxTask.class.getSimpleName();

    private String activatingCondition = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Executed task HTTP NAQ \\([0-9]*\\) \\{[0-9A-Za-z]*\\}, originator octcpd \\[[0-9A-Z]*\\]";
    Pattern patternActivatingCondition = Pattern.compile(activatingCondition);

    @Override
    public NaqForTrxWrapper parseLine(String line) {
        Matcher matcherActivatingConditions = patternActivatingCondition.matcher(line);
        if(matcherActivatingConditions.find()){
            NaqForTrxWrapper naqForTrxWrapper = new NaqForTrxWrapper();
            setTimestampToWrapper(naqForTrxWrapper, matcherActivatingConditions);
            return naqForTrxWrapper;
        }
        return null;
    }
}
