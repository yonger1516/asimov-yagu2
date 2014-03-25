package com.seven.asimov.it.utils.logcat.tasks.policyTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.ReceivedPolicyWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: amykytenko_cv
 * Date: 6/4/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReceivedPolicyTask extends Task<ReceivedPolicyWrapper> {
    private static final String TAG = ReceivedPolicyTask.class.getSimpleName();

    private static final String RECEIVED_POLICY_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Received policy size: ([0-9]*) version: ([0-9]*)";
    private static final Pattern receivedPolicyPattern = Pattern.compile(RECEIVED_POLICY_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected ReceivedPolicyWrapper parseLine(String line) {
        Matcher matcher = receivedPolicyPattern.matcher(line);
        if (matcher.find()) {
            ReceivedPolicyWrapper wrapper = new ReceivedPolicyWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setSize(matcher.group(3));
            wrapper.setVersion(matcher.group(4));
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s = "05-27 02:36:23.992: D/Asimov::Java::PolicyServiceImpl(559): 2013/05/27 02:36:23.996000 EEST 66 [DEBUG] [com.seven.pms.client.PolicyServiceImpl] Received policy size: 13 version: 10194";
        ReceivedPolicyTask task = new ReceivedPolicyTask();
        ReceivedPolicyWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
    }

}
