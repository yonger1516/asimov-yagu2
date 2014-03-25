package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.PackDumpSuccessWrapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackDumpSuccessTask extends Task<PackDumpSuccessWrapper> {

    private static final String TAG = PackDumpSuccessTask.class.getSimpleName();

    private static final String PACK_DUMP_SUCCESS_REGEXP =
            "(201[2-9]/[0-9]*/[0-9]*\\s[0-9]*\\:[0-9]*\\:[0-9]*\\.[0-9]*).([A-Z]*).*Report pack dump executed and sent successfully";

    private static final Pattern packDumpSuccessPattern = Pattern.compile(PACK_DUMP_SUCCESS_REGEXP, Pattern.CASE_INSENSITIVE);

    @Override
    public PackDumpSuccessWrapper parseLine(String line) {
        Matcher matcher = packDumpSuccessPattern.matcher(line);
        if (matcher.find()) {
            PackDumpSuccessWrapper wrapper = new PackDumpSuccessWrapper();
            setTimestampToWrapper(wrapper, matcher);
            return wrapper;
        }
        return null;
    }
}
