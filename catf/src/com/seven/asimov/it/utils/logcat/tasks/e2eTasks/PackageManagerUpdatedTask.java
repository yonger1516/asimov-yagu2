package com.seven.asimov.it.utils.logcat.tasks.e2eTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PackageManagerUpdatedWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageManagerUpdatedTask extends Task<PackageManagerUpdatedWrapper> {
    private static final String TAG = PackageManagerUpdatedWrapper.class.getSimpleName();

    private static final String PACKAGE_MANAGER_UPDATED_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Package manager updated";
    private static final Pattern packageManagerUpdatedPattern = Pattern.compile(PACKAGE_MANAGER_UPDATED_REGEXP);

    @Override
    protected PackageManagerUpdatedWrapper parseLine(String line) {
        Matcher matcher = packageManagerUpdatedPattern.matcher(line);
        if (matcher.find()) {
            PackageManagerUpdatedWrapper wrapper = new PackageManagerUpdatedWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
