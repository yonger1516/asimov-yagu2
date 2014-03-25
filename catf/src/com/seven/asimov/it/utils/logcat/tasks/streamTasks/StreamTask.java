package com.seven.asimov.it.utils.logcat.tasks.streamTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.StreamWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamTask extends Task<StreamWrapper> {

    public String regexspDispatcher = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Executed task OC Scheduler Task ([A-Z]*).*";
    public String regexspOC = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Executed task ([HTTPS]*) ([A-Z]*).*originator ([a-z]*) .([0-9A-Z]*).";
    public String regexspNSC = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* - Executed task ([A-Z]*).*originator ([a-z]*) .([0-9A-Z]*).*";
    Pattern patternDispatcher = Pattern.compile(regexspDispatcher);
    Pattern patternOC = Pattern.compile(regexspOC);
    Pattern patternNSC = Pattern.compile(regexspNSC);

    @Override
    protected StreamWrapper parseLine(String line) {
        Matcher matcherDispatcher = patternDispatcher.matcher(line);
        Matcher matcherOC = patternOC.matcher(line);
        Matcher matcherNSC = patternNSC.matcher(line);

        if(matcherDispatcher.find()) {
            StreamWrapper streamWrapper = new StreamWrapper();
            setTimestampToWrapper(streamWrapper, matcherDispatcher);
            streamWrapper.setTask(matcherDispatcher.group(3));
            return streamWrapper;
        }
        if (matcherOC.find()) {
            StreamWrapper streamWrapper = new StreamWrapper();
            setTimestampToWrapper(streamWrapper, matcherOC);
            streamWrapper.setTask(matcherOC.group(4));
            streamWrapper.setProtocol(matcherOC.group(3));
            streamWrapper.setOriginator(matcherOC.group(5));
            return streamWrapper;
        }
        if(matcherNSC.find()) {
            StreamWrapper streamWrapper = new StreamWrapper();
            setTimestampToWrapper(streamWrapper, matcherNSC);
            streamWrapper.setTask(matcherNSC.group(3));
            return streamWrapper;
        }
        return null;
    }
}
