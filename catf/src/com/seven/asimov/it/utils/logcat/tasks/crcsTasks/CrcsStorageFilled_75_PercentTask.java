package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.seven.asimov.it.utils.logcat.wrappers.CrcsStorageFilled_75_Wrapper;
import com.seven.asimov.it.utils.logcat.tasks.Task;
public class CrcsStorageFilled_75_PercentTask extends Task<CrcsStorageFilled_75_Wrapper> {

    private static final String TAG = CrcsStorageFilled_75_PercentTask.class.getSimpleName();

    private static final String CRCS_STORAGE_FILLED_75_PERCENT_REGEX = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*CRCS storage is already filled on ([0-9]*).*\\(([0-9]*).*";

    private static final Pattern crcsStorageFilledPattern = Pattern.compile(CRCS_STORAGE_FILLED_75_PERCENT_REGEX, Pattern.CASE_INSENSITIVE);

    @Override
    protected CrcsStorageFilled_75_Wrapper parseLine(String line) {
        Matcher matcher = crcsStorageFilledPattern.matcher(line);
        if (matcher.find()) {
            CrcsStorageFilled_75_Wrapper wrapper = new CrcsStorageFilled_75_Wrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setPercent(Integer.parseInt(matcher.group(3)));
            wrapper.setRecordCount(Integer.parseInt(matcher.group(4)));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
