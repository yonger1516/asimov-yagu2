package com.seven.asimov.it.utils.logcat.tasks.httpsTasks;


import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.VerdictForFCLWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerdictForFCLTask extends Task<VerdictForFCLWrapper> {
    boolean FCP;
    public VerdictForFCLTask(boolean FCP){
        this.FCP = FCP;
    }

    public boolean getFCP(){
        return FCP;
    }

    private static final String FCP_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).*- FC \\[[A-Z0-9]*\\]: verdict (FCP)";
    private static Pattern fcpPattern = Pattern.compile(FCP_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final String FCN_REGEXP =
            "(201[2-9]/[0-9][0-9]/[0-9][0-9] [0-9]*:[0-9][0-9]:[0-9][0-9].[0-9]*) ([A-Z]*).*: verdict (FCN) \\(IP ([0-9.]*).*capabilities ([0-9]*).*";
    private static Pattern fcnPattern = Pattern.compile(FCN_REGEXP, Pattern.CASE_INSENSITIVE);


    protected VerdictForFCLWrapper parseLine(String line) {
        Matcher matcher;
        if (FCP){ matcher = fcpPattern.matcher(line); }
        else { matcher = fcnPattern.matcher(line); }

        if (matcher.find()) {
            VerdictForFCLWrapper wrapper = new VerdictForFCLWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setVerdict(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
