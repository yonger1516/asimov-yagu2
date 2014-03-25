package com.seven.asimov.it.utils.logcat.tasks.dispatcherTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.DefaultConfigReInstalledWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultConfigReInstalledTask extends Task<DefaultConfigReInstalledWrapper> {

    private static final String TAG = DefaultConfigReInstalledTask.class.getSimpleName();
    private static final String CONFIG_DELETED = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*:[0-9]*:[0-9]*\\.[0-9]*).([A-Z]*).* delete /data/misc/openchannel/dispatchers_default.cfg  returned true";
    private static final String CONFIG_INSTALLED = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*:[0-9]*:[0-9]*\\.[0-9]*).([A-Z]*).* extracted dispatchers_default.cfg to /data/misc/openchannel/dispatchers_default.cfg";
    private static final Pattern configDeletedPattern = Pattern.compile(CONFIG_DELETED, Pattern.CASE_INSENSITIVE);
    private static final Pattern configInstalledPattern = Pattern.compile(CONFIG_INSTALLED, Pattern.CASE_INSENSITIVE);

    @Override
    protected DefaultConfigReInstalledWrapper parseLine(String line) {
        Matcher confDelMatcher = configDeletedPattern.matcher(line);
        Matcher confInstMatcher = configInstalledPattern.matcher(line);
        if (confDelMatcher.find()) {
            DefaultConfigReInstalledWrapper wrapper = new DefaultConfigReInstalledWrapper();
            setTimestampToWrapper(wrapper, confDelMatcher);
            wrapper.setConfigDeleted(true);
            return wrapper;
        }
        if (confInstMatcher.find()) {
            DefaultConfigReInstalledWrapper wrapper = new DefaultConfigReInstalledWrapper();
            setTimestampToWrapper(wrapper, confInstMatcher);
            wrapper.setConfigInstalled(true);
            return wrapper;
        }
        return null;
    }
}
