package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KAactivationTask extends Task<KeepaliveWrapper> {
    //08-04 12:17:49.310 D/Asimov::JNI::OCEngine( 3630): 2013/08/04 12:17:49.316197 EEST 9088 [DEBUG]	[conditions.cpp:592] (0) - Keepalive condition, is_active=false: (group=enter, script=0x5dbc34b0)
    public String KAactivation = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.[a-z_?]*.[a-z]*.[0-9]*.\\s.[0-9]. - Keepalive condition, is_active=[a-z]*:\\s*.*";
    public Pattern KAactivationPattern = Pattern.compile(KAactivation);

    @Override
    public KeepaliveWrapper parseLine(String line) {
        Matcher matcher = KAactivationPattern.matcher(line);
        if(matcher.find()) {
            KeepaliveWrapper keepaliveWrapper = new KeepaliveWrapper();
            setTimestampToWrapper(keepaliveWrapper, matcher);
            return keepaliveWrapper;
        }
        return null;
    }
}
