package com.seven.asimov.it.utils.logcat.tasks.cachingTasks;

import com.seven.asimov.it.utils.logcat.wrappers.CashEntryWraper;
import com.seven.asimov.it.utils.logcat.tasks.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CERemovedFromFSTask extends Task <CashEntryWraper> {
    private static final String REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*\\sCE\\s+(\\S+).(removed from file system)";
    private static Pattern pattern = Pattern.compile(REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    protected CashEntryWraper parseLine(String line) {

        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            CashEntryWraper wrapper = CashEntryWraper.getInstanse();
            setTimestampToWrapper(wrapper, matcher);
//            if (wrapper.getCeId() == null) {
//                wrapper.setCeId(matcher.group(3));
//            }

            wrapper.setDelitedFromFS(true);

            return wrapper;
        }
        return null;
    }
}
