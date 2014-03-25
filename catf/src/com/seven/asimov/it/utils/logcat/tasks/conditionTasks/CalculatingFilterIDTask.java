package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CalculatingFilterIDTask extends Task<KeepaliveWrapper> {

    String KA_calkulatingID = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.[a-z_?]*.[a-z]*.[0-9]*.\\s.[0-9].\\s*- For filter uid=[0-9]* calculated id.*";
    Pattern KA_caPattern = Pattern.compile(KA_calkulatingID);
    @Override
    public KeepaliveWrapper parseLine(String line) {

        Matcher matcher = KA_caPattern.matcher(line);
        if(matcher.find()) {
            KeepaliveWrapper keepaliveWrapper = new KeepaliveWrapper();
            setTimestampToWrapper(keepaliveWrapper, matcher);
            return  keepaliveWrapper;
        }
        return null;
    }
}
