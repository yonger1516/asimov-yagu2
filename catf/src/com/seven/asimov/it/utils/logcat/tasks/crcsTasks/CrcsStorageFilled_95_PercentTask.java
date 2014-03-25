package com.seven.asimov.it.utils.logcat.tasks.crcsTasks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.seven.asimov.it.utils.logcat.tasks.Task;
import com.seven.asimov.it.utils.logcat.wrappers.CrcsStorageFilled_95_Wrapper;

public class CrcsStorageFilled_95_PercentTask extends Task<CrcsStorageFilled_95_Wrapper> {

    private static final String TAG = CrcsStorageFilled_95_PercentTask.class.getSimpleName();

    private static final String CRCS_STORAGE_FILLED_75_PERCENT_REGEX = "(201[2-9]/[0-9]*/[0-9]* [0-9]*:[0-9]*:[0-9]*.[0-9]*) ([A-Z]*).*CRCS storage size was close to ([0-9]*).* percents, removed = ([0-9]*).* records";

    private static final Pattern crcsStorageFilledPattern = Pattern.compile(CRCS_STORAGE_FILLED_75_PERCENT_REGEX, Pattern.CASE_INSENSITIVE);

    @Override
    protected CrcsStorageFilled_95_Wrapper parseLine(String line) {
        Matcher matcher = crcsStorageFilledPattern.matcher(line);
        if (matcher.find()) {
            CrcsStorageFilled_95_Wrapper wrapper = new CrcsStorageFilled_95_Wrapper();
            setTimestampToWrapper(wrapper, matcher);
            wrapper.setPercent(Integer.parseInt(matcher.group(3)));
            wrapper.setRecordsDeleted(Integer.parseInt(matcher.group(4)));
            return wrapper;
        }
        return null;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
