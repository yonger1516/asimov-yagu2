package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FTMmessageTask extends Task<KeepaliveWrapper> {

    String message = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.[a-z_?]*.[a-z]*.[0-9]*.\\s.[0-9].\\s*.\\s*Sending FTM messages\\s.[0-9]*. to dispatchers";
    Pattern messagePattern = Pattern.compile(message);
    @Override
    public KeepaliveWrapper parseLine(String line) {

        Matcher matcher = messagePattern.matcher(line);
        if(matcher.find()) {
            KeepaliveWrapper keepaliveWrapper = new KeepaliveWrapper();
            setTimestampToWrapper(keepaliveWrapper, matcher);
            return  keepaliveWrapper;
        }
        return null;
    }
}
