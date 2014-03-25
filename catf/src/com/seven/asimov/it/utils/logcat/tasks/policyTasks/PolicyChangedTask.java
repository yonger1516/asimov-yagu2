package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyChangedWrapper;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyChangedTask extends Task<PolicyChangedWrapper> {
    private static final String TAG = PolicyChangedTask.class.getSimpleName();

    private static final String POLICY_CHANGED_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Policy changed, important: ([a-zA-Z]*)";
    private static final Pattern policyChangedPattern = Pattern.compile(POLICY_CHANGED_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected PolicyChangedWrapper parseLine(String line) {
        Matcher matcher = policyChangedPattern.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            PolicyChangedWrapper wrapper = new PolicyChangedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setImportant(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
