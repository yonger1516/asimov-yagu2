package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Deprecated
//use PolicyAppliedTask instead
public class PolicyAddedTask extends Task<PolicyWrapper> {
    private static final String TAG = PolicyAddedTask.class.getSimpleName();

    private static final String POLICY_ADDED_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* Adding a policy node id ([0-9]*).* name (\\w*).* value (\\w*)";

    private static final String POLICY_ADDED_REGEXP1 =
            "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).* Adding a policy node id ([0-9]*), level [0-9]*:\\s*\\'(.*)\\':\\'(.*)\\'";

    private static final Pattern radioStateRecordsPattern = Pattern.compile(POLICY_ADDED_REGEXP, Pattern.CASE_INSENSITIVE);
    private static final Pattern radioStateRecordsPattern1 = Pattern.compile(POLICY_ADDED_REGEXP1, Pattern.CASE_INSENSITIVE);

    @Override
    public PolicyWrapper parseLine(String line) {
        Matcher matcher = radioStateRecordsPattern.matcher(line);
        if (matcher.find()) {
            PolicyWrapper wrapper = new PolicyWrapper();
            setTimestampToWrapper(wrapper, matcher);
//            Log.d(TAG, "parseLine found line " + matcher.group(1));
            wrapper.setId(Integer.parseInt(matcher.group(3)));
            wrapper.setName(matcher.group(4));
            wrapper.setValue(matcher.group(5));
            return wrapper;
        }
        matcher = radioStateRecordsPattern1.matcher(line);
        if (matcher.find()) {
            PolicyWrapper wrapper = new PolicyWrapper();
            setTimestampToWrapper(wrapper, matcher);
//            Log.d(TAG, "parseLine found line " + matcher.group(1));
            wrapper.setId(Integer.parseInt(matcher.group(3)));
            wrapper.setName(matcher.group(4));
            wrapper.setValue(matcher.group(5));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
