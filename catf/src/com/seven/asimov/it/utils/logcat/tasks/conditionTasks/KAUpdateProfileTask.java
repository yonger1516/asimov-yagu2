package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KAUpdateProfileTask extends Task<KeepaliveWrapper> {


    public String KA_updateAppProfileWithStatus ="(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.[a-z_?]*.[a-z]*.[0-9]*.\\s.[0-9].\\s*.\\s*AppProfile\\s*.([0-9]*).\\sdetect KA for TRX .([0-9A-Z]*).*";
    public Pattern KA_updateAppProfileWithStatusPattern = Pattern.compile(KA_updateAppProfileWithStatus);

    @Override
    public KeepaliveWrapper parseLine(String line) {
        Matcher matcher = KA_updateAppProfileWithStatusPattern.matcher(line);
        if(matcher.find()) {
            KeepaliveWrapper keepaliveWrapper = new KeepaliveWrapper();
            setTimestampToWrapper(keepaliveWrapper, matcher);

            return keepaliveWrapper;
        }
        return null;
    }

}
