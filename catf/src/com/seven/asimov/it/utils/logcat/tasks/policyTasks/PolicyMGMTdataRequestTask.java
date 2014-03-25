package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import android.util.Log;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PolicyMGMTdataRequestWrapper;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolicyMGMTdataRequestTask extends Task<PolicyMGMTdataRequestWrapper> {
    private static final String TAG = PolicyMGMTdataRequestTask.class.getSimpleName();

    private static final String SENDING_A_POLICY_MGMT_DATA_REQUEST_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Sending a policy mgmt data request .0-([a-zA-Z0-9]*)-0.*hint.[0-9]*";
    private static final Pattern sendingPolicyMGMTdataRequestPattern = Pattern.compile(SENDING_A_POLICY_MGMT_DATA_REQUEST_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected PolicyMGMTdataRequestWrapper parseLine(String line) {
        Matcher matcher = sendingPolicyMGMTdataRequestPattern.matcher(line);
        if (matcher.find()) {
            TimeZone.setDefault(TimeZone.getTimeZone("GTM"));
            Log.d(TAG, "Set timezone to GMT");
            PolicyMGMTdataRequestWrapper wrapper = new PolicyMGMTdataRequestWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setZ7TP(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
