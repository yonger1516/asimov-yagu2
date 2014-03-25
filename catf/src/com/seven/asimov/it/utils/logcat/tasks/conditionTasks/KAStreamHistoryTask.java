package com.seven.asimov.it.utils.logcat.tasks.conditionTasks;

import com.seven.asimov.it.utils.logcat.wrappers.KeepaliveWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KAStreamHistoryTask extends Task<KeepaliveWrapper> {

    public String KA_StreamHistory= "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).[0-9]*..[A-Z]*.\\s.app_profile.cpp:\\d{3}...[0-9]. -.[0-9]*.\\s*.\\s*.\\s*([0-9A-Z]*)\\s*.\\s*([0-9A-Z]*)\\s*.\\s*([0-9A-Z]*)\\s*.\\s*([A-Z]*)\\s*.\\s*([0-9]+)\\s*.\\s([0-9]+)\\s*.\\s*([0-9]+)\\s*.\\s*([0-9]+)\\s.\\s*([A-Z]*\\s?[A-Z]{0,3})\\s*.\\s*([0-9]+)";
    public Pattern KA_StreamHistoryPattern = Pattern.compile(KA_StreamHistory);

    @Override
    public KeepaliveWrapper parseLine(String line) {
        Matcher matcher = KA_StreamHistoryPattern.matcher(line);
        if(matcher.find()) {
            KeepaliveWrapper keepaliveWrapper = new KeepaliveWrapper();
            setTimestampToWrapper(keepaliveWrapper, matcher);
            keepaliveWrapper.setAppUid(matcher.group(3));
            keepaliveWrapper.setDelay(matcher.group(10));
//            keepaliveWrapper.setKAstate(matcher.group(13));
//            keepaliveWrapper.setKAweight(matcher.group(14));
            keepaliveWrapper.setCsmId(matcher.group(4));
            keepaliveWrapper.setTrxID(matcher.group(7));
            keepaliveWrapper.setTRXkey(matcher.group(5));
            keepaliveWrapper.setMsg(matcher.group(6));
            keepaliveWrapper.setBfc(Integer.parseInt(matcher.group(9)));
            keepaliveWrapper.setKAstate(matcher.group(11).trim());
            return keepaliveWrapper;
        }
        return null;
    }
}
