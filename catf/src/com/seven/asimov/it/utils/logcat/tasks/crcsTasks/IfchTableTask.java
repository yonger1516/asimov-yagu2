package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.wrappers.IfchTableWrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfchTableTask extends Task<IfchTableWrapper> {

    private static final String TAG = IfchTableTask.class.getSimpleName();

    private static final String IFCH_TABLE_REGEXP = "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Za-z]*).*IFCH\\:.[0-9]*\\:[0-9\\.\\s]*([a-z_\\-]*)";
    private static final Pattern ifchTablePatern = Pattern.compile(IFCH_TABLE_REGEXP, Pattern.CASE_INSENSITIVE);


    protected IfchTableWrapper parseLine(String line) {
        Matcher matcher = ifchTablePatern.matcher(line);
        if (matcher.find()) {
            IfchTableWrapper wrapper = new IfchTableWrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setInterfaceType(matcher.group(3));
            return wrapper;
        }
        return null;
    }
}
