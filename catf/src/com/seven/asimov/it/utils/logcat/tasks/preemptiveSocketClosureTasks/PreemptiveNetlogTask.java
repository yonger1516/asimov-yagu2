package com.seven.asimov.it.utils.logcat.tasks.preemptiveSocketClosureTasks;


import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PreemptiveWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PreemptiveNetlogTask extends Task<PreemptiveWrapper> {
    private static final String TAG = PreemptiveNetlogTask.class.getSimpleName();
    private String preemptiveNetlogRegexp =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*NetLog.*%s.*";
    private Pattern preemptiveNetlogPattern;

    public PreemptiveNetlogTask(String socketClosureType){
        preemptiveNetlogRegexp = String.format(preemptiveNetlogRegexp, socketClosureType);
        preemptiveNetlogPattern = Pattern.compile(preemptiveNetlogRegexp, Pattern.CASE_INSENSITIVE);
        Log.v(TAG, "Preemptive Netlog Regexp= " + preemptiveNetlogRegexp);
    }

    @Override
    protected PreemptiveWrapper parseLine(String line) {
        Matcher matcher = preemptiveNetlogPattern.matcher(line);
        if(matcher.find()){
            PreemptiveWrapper wrapper = new PreemptiveWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
