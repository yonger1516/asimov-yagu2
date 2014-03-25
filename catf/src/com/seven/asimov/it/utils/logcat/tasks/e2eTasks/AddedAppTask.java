package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.AddedAppWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddedAppTask extends Task<AddedAppWrapper> {
    private static final String TAG = AddedAppTask.class.getSimpleName();

    private static final String ADDED_APK_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Added Applications: (.*)";
    private static final Pattern addedApkPattern = Pattern.compile(ADDED_APK_REGEXP);

    @Override
    protected AddedAppWrapper parseLine(String line) {
        Matcher matcher = addedApkPattern.matcher(line);
        if (matcher.find()) {
            AddedAppWrapper wrapper = new AddedAppWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setAppName(matcher.group(3));
            return wrapper;
        }
        return null;
    }

    public static void main(String[] args) {
        String s = "05-13 07:23:22.953: I/Asimov::Java::PolicyServiceImpl(13275): 2013/05/13 07:23:22.956000 EEST 157 [INFO] [com.seven.pms.client.PolicyServiceImpl] Added Applications: [{1082386=\"com.seven.asimov.test.tool\", 1082387=\"485269\", 1082393=\"2.0.485269\"}] size: 1\n";
        AddedAppTask task = new AddedAppTask();
        AddedAppWrapper wrapper = task.parseLine(s);
        System.out.println(wrapper);
        System.out.println(wrapper.getAppName().contains("com.seven.asimov.test.tool"));
    }
}
