package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyAppliedTask extends Task<PolicyWrapper> {
    private static final String TAG = PolicyAppliedTask.class.getSimpleName();

    private String policyAppliedRegexp =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* Adding a policy node id ([0-9]*), level [0-9]*:\\s*\\'%s\\':\\'%s\\'";
    private  Pattern radioStateRecordsPattern;


    private String name;
    private String value;

    public PolicyAppliedTask(String name, String value) {
        this.name = name;
        this.value = value;

        policyAppliedRegexp = String.format(policyAppliedRegexp, name, value);
        Log.v(TAG, "policyAppliedRegexp=" + policyAppliedRegexp);
        radioStateRecordsPattern = Pattern.compile(policyAppliedRegexp, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public PolicyWrapper parseLine(String line) {
        Matcher matcher = radioStateRecordsPattern.matcher(line);
        if (matcher.find()) {
            PolicyWrapper wrapper = new PolicyWrapper();
            setTimestampToWrapper(wrapper, matcher);
//            Log.d(TAG, "parseLine found line " + matcher.group(1));
            wrapper.setId(Integer.parseInt(matcher.group(3)));
            wrapper.setName(name);
            wrapper.setValue(value);
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
