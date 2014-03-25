package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.NetworktypeConditionWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworktypeConditionTask extends Task<NetworktypeConditionWrapper> {

    private String activatingCondition = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*\\'([0-9a-zA-Z]*)\\' network family is reachable now. Activating condition...";
    Pattern patternActivatingCondition = Pattern.compile(activatingCondition);

    public NetworktypeConditionWrapper parseLine(String line) {
        Matcher matcherActivatingConditions = patternActivatingCondition.matcher(line);
        if(matcherActivatingConditions.find()){
            NetworktypeConditionWrapper networktypeConditionWrapper = new NetworktypeConditionWrapper();
            setTimestampToWrapper(networktypeConditionWrapper, matcherActivatingConditions);
            networktypeConditionWrapper.setNetworkType(matcherActivatingConditions.group(3));
            return networktypeConditionWrapper;
        }
        return null;
    }
}
