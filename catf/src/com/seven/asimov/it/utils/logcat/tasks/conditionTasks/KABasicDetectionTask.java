package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;


import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KABasicDetectionTask extends Task<KeepaliveWrapper> {

    public String KABasicDetection = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.[a-z_?]*.[a-z]*.[0-9]*.\\s.[0-9]. - Default AppProfile .uid=([0-9]*). has been created";
    public Pattern KABasicDetectionPattern = Pattern.compile(KABasicDetection);


    @Override
    public KeepaliveWrapper parseLine(String line) {
        Matcher matcher = KABasicDetectionPattern.matcher(line);
        if(matcher.find()) {
            KeepaliveWrapper keepaliveWrapper = new KeepaliveWrapper();
            setTimestampToWrapper(keepaliveWrapper, matcher);
            keepaliveWrapper.setAppUid(matcher.group(3));
            return keepaliveWrapper;
        }
        return null;
    }
}
