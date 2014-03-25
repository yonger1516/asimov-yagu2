package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.AddingAppWrapper;
import java.lang.String;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddingAppTask extends Task<AddingAppWrapper> {
    private static final String TAG = AddingAppWrapper.class.getSimpleName();

    private static Pattern appAddingPattern;

    public AddingAppTask(String appPackageName) {
        String APP_ADDING_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Adding app " + appPackageName + ", UID ([0-9]*)";
        appAddingPattern = Pattern.compile(APP_ADDING_REGEXP);
    }

    @Override
    protected AddingAppWrapper parseLine(String line) {
        Matcher matcher = appAddingPattern.matcher(line);
        if (matcher.find()) {
            AddingAppWrapper wrapper = new AddingAppWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setUuid(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
